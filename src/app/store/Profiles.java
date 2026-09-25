package app.store;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.openlca.commons.Strings;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfiles;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.editors.Editors;
import app.navi.Navigator;

/// The registry and storage of the EPD profiles that are available in the
/// application.
///
/// The built-in profiles are defined in the `olca-ilcd` library as an enum.
/// They are bundled as XML resources in the library and cannot be modified
/// here. User defined profiles are stored as XML files in the `profiles`
/// folder of the workspace. They are created, edited and deleted by the user.
///
/// This class is the single source of truth for the available
/// profiles. Note that the static registry of the `olca-ilcd` library
/// (`EpdProfiles#add`) is not used: it has no way to remove a profile
/// again and user profiles should never shadow a built-in profile.
public final class Profiles {

	private static final Logger log = LoggerFactory.getLogger(Profiles.class);

	private static final Map<String, EpdProfile> userProfiles =
		new ConcurrentHashMap<>();

	/// When set, this folder is used instead of the workspace folder for the
	/// user defined profiles. This is only used in tests.
	private static File dirOverride;

	/// Uses the given folder for the user defined profiles instead of the
	/// workspace folder. Pass `null` to use the workspace folder again. This
	/// is only used in tests.
	static void useDir(File dir) {
		dirOverride = dir;
	}

	private Profiles() {
	}

	/// Returns all available profiles: the built-in profiles in the order of
	/// their definition, followed by the user defined profiles sorted by name.
	public static List<EpdProfile> getAll() {
		var list = new ArrayList<EpdProfile>();
		for (var v : EpdProfiles.values()) {
			list.add(v.get());
		}
		list.addAll(userProfiles());
		return list;
	}

	/// Returns all available profiles sorted by name. This is the order in
	/// which they are shown in selection boxes.
	public static List<EpdProfile> getAllSorted() {
		var list = getAll();
		list.sort((p1, p2) ->
			Strings.compareIgnoreCase(p1.getName(), p2.getName()));
		return list;
	}

	/// Returns the user defined profiles sorted by name.
	public static List<EpdProfile> userProfiles() {
		var list = new ArrayList<>(userProfiles.values());
		list.sort((p1, p2) ->
			Strings.compareIgnoreCase(p1.getName(), p2.getName()));
		return list;
	}

	/// Returns the profile with the given ID. This can be a built-in profile
	/// or a user defined profile, or `null` when no such profile exists.
	public static EpdProfile get(String id) {
		if (Strings.isBlank(id))
			return null;
		var user = userProfiles.get(id);
		return user != null
			? user
			: builtIn(id);
	}

	/// Returns `true` when the given profile is a built-in profile that
	/// cannot be modified by the user.
	public static boolean isBuiltIn(EpdProfile profile) {
		return profile != null && isBuiltIn(profile.getId());
	}

	/// Returns `true` when the given ID is the ID of a built-in profile.
	public static boolean isBuiltIn(String id) {
		return builtIn(id) != null;
	}

	/// Tries to find a profile that matches the indicators of the given EPD.
	/// The default profile has the highest priority, followed by the other
	/// built-in profiles, so that a user defined profile can never shadow a
	/// built-in profile. Returns `null` when no profile matches.
	public static EpdProfile of(Process epd) {
		if (epd == null)
			return null;
		var defaultProfile = EpdProfiles.getDefault();
		if (EpdProfiles.matches(epd, defaultProfile))
			return defaultProfile;
		for (var v : EpdProfiles.values()) {
			var profile = v.get();
			if (Objects.equals(profile, defaultProfile))
				continue;
			if (EpdProfiles.matches(epd, profile))
				return profile;
		}
		for (var profile : userProfiles()) {
			if (EpdProfiles.matches(epd, profile))
				return profile;
		}
		return null;
	}

	/// Loads all user defined profiles from the workspace. This is called
	/// when the application starts.
	public static void load() {
		userProfiles.clear();
		readFrom();
	}

	/// Reloads the user defined profiles from the workspace. This should be
	/// called when the profile files may have been changed outside of the
	/// application.
	public static void reload() {
		load();
	}

	/// The folder where the user defined profiles are stored. It is created
	/// when it does not exist yet. In tests it can be replaced with [useDir].
	static File dir() {
		if (dirOverride != null)
			return dirOverride;
		var dir = new File(App.workspaceFolder(), "profiles");
		if (!dir.exists()) {
			try {
				Files.createDirectories(dir.toPath());
			} catch (Exception e) {
				log.error("failed to create profile folder {}", dir, e);
			}
		}
		return dir;
	}

	/// Creates a new, empty user defined profile, saves it in the workspace
	/// and returns it. Returns `null` when it could not be saved.
	public static EpdProfile create() {
		var profile = new EpdProfile()
			.withId(UUID.randomUUID().toString())
			.withName(M.NewProfile);
		normalize(profile);
		return save(profile)
			? profile
			: null;
	}

	/// Creates a copy of the given profile with a new ID and name, saves it in
	/// the workspace and returns it. Returns `null` when it could not be saved.
	public static EpdProfile copyOf(EpdProfile profile) {
		if (profile == null)
			return null;
		var copy = profile.copy();
		if (copy == null)
			return null;
		copy.withId(UUID.randomUUID().toString());
		var name = profile.getName();
		copy.withName(Strings.isBlank(name)
			? M.NewProfile
			: M.CopyOfProfile + " " + name);
		normalize(copy);
		return save(copy)
			? copy
			: null;
	}

	/// Writes the given profile to the workspace and registers it. Returns
	/// `false` when the profile is a built-in profile or could not be written.
	public static boolean save(EpdProfile profile) {
		var id = profile != null ? profile.getId() : null;
		if (Strings.isBlank(id))
			return false;
		if (isBuiltIn(id)) {
			log.warn("refusing to overwrite the built-in profile {}", id);
			return false;
		}
		var file = new File(dir(), id + ".xml");
		try {
			EpdProfiles.write(profile, file);
		} catch (Exception e) {
			log.error("failed to write profile to {}", file, e);
			return false;
		}
		userProfiles.put(id, profile);
		Navigator.refreshProfiles();
		return true;
	}

	/// Deletes the given user defined profile. The profile file is removed
	/// from the workspace and the profile is unregistered. When the deleted
	/// profile was set as the default profile, the default profile is reset to
	/// the built-in default. Returns `false` when the profile is a built-in
	/// profile or the file could not be deleted.
	public static boolean delete(EpdProfile profile) {
		var id = profile != null ? profile.getId() : null;
		if (Strings.isBlank(id) || isBuiltIn(id))
			return false;

		var file = new File(dir(), id + ".xml");
		if (file.exists()) {
			try {
				Files.delete(file.toPath());
			} catch (Exception e) {
				log.error("failed to delete profile file {}", file, e);
				return false;
			}
		}

		userProfiles.remove(id);

		var settings = App.settings();
		if (Objects.equals(settings.profile, id)) {
			settings.profile = EpdProfiles.getDefault().getId();
			settings.save(App.getWorkspace());
		}

		Editors.close(profile);
		Navigator.refreshProfiles();
		return true;
	}

	/// Reads all user defined profiles from the storage folder. Invalid files
	/// and files that would shadow a built-in profile or another user defined
	/// profile are skipped.
	private static void readFrom() {
		var files = dir().listFiles();
		if (files == null)
			return;
		for (var file : files) {
			if (!file.isFile() || !file.getName().endsWith(".xml"))
				continue;
			EpdProfile profile;
			try {
				profile = EpdProfiles.read(file);
			} catch (Exception e) {
				log.error("failed to read profile from {}", file, e);
				continue;
			}
			if (profile == null)
				continue;
			var id = profile.getId();
			if (Strings.isBlank(id)) {
				log.warn("skipping profile without ID: {}", file);
				continue;
			}
			if (isBuiltIn(id)) {
				log.warn("skipping profile that shadows a built-in profile: {}", file);
				continue;
			}
			if (userProfiles.containsKey(id)) {
				log.warn("skipping profile with duplicate ID {}: {}", id, file);
				continue;
			}
			normalize(profile);
			userProfiles.put(id, profile);
		}
	}

	/// Ensures that the module and indicator lists of a profile are not null.
	/// The XML reader of the library may create profiles without these lists.
	/// Several components (like the result mapping of an EPD) expect that these
	/// lists can be iterated without a null check.
	private static void normalize(EpdProfile profile) {
		if (profile == null)
			return;
		profile.withModules();
		profile.withIndicators();
	}

	/// Returns the built-in profile with the given ID or `null` when no such
	/// profile exists. Note that the registry of the library is not used here:
	/// it can be modified from the outside and has no way to remove a profile
	/// again.
	private static EpdProfile builtIn(String id) {
		if (Strings.isBlank(id))
			return null;
		for (var v : EpdProfiles.values()) {
			if (Objects.equals(id, v.name()))
				return v.get();
		}
		return null;
	}
}
