package app.store;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.Module;
import epd.util.Strings;

public final class EpdProfiles {

	private static final String DEFAULT = "EN_15804";

	private static EpdProfile profile;

	private EpdProfiles() {
	}

	/**
	 * Set the given profile as the default profile of the application.
	 */
	public static void set(EpdProfile p) {
		profile = p;
		if (p == null)
			return;
		if (!Strings.nullOrEqual(p.id, App.settings().profile)) {
			App.settings().profile = p.id;
			App.settings().save();
		}
	}

	/** Get the active profile of the application. */
	public static EpdProfile get() {
		if (profile != null)
			return profile;
		String id = App.settings().profile;
		if (id == null) {
			id = DEFAULT;
		}
		profile = fromFile(id);
		if (profile != null)
			return profile;

		// loading from file failed; load default profile
		File defaultFile = file(DEFAULT);
		if (defaultFile.exists()) {
			profile = fromFile(DEFAULT);
		}
		if (profile == null) {
			InputStream stream = EpdProfiles.class
					.getResourceAsStream(DEFAULT + ".json");
			profile = Json.read(stream, EpdProfile.class);
			save(profile);
		}
		if (!Objects.equals(DEFAULT, App.settings().profile)) {
			App.settings().profile = DEFAULT;
			App.settings().save();
		}

		if (profile == null) {
			// this should never happen
			profile = new EpdProfile();
			profile.id = DEFAULT;
			profile.name = DEFAULT;
			Logger log = LoggerFactory.getLogger(EpdProfiles.class);
			log.error("failed to load an EPD profile; even the default");
		}

		// TODO: sync with indicator data sets

		return profile;
	}

	public static EpdProfile get(String id) {
		if (id == null)
			return null;
		if (Strings.nullOrEqual(id, App.settings().profile))
			return get();
		File f = file(id);
		if (f == null || !f.exists())
			return null;
		return Json.read(f, EpdProfile.class);
	}

	/** Get all EPD profiles from the workspace. */
	public static List<EpdProfile> getAll() {
		File dir = new File(App.workspace, "epd_profiles");
		if (!dir.exists())
			return Collections.emptyList();
		List<EpdProfile> profiles = new ArrayList<>();
		for (File f : dir.listFiles()) {
			EpdProfile p = Json.read(f, EpdProfile.class);
			if (p != null) {
				profiles.add(p);
			}
		}
		return profiles;
	}

	/** Get the indicators from the default profile. */
	public static List<Indicator> indicators() {
		return get().indicators;
	}

	public static List<Module> modules() {
		return get().modules;
	}

	/** Save the given profile in the workspace. */
	public static void save(EpdProfile profile) {
		if (profile == null || profile.id == null)
			return;
		File file = file(profile.id);
		Json.write(profile, file);
	}

	private static EpdProfile fromFile(String id) {
		File file = file(id);
		if (file.exists()) {
			profile = Json.read(file, EpdProfile.class);
			if (profile != null)
				return profile;
		}
		return null;
	}

	private static File file(String id) {
		File dir = new File(App.workspace, "epd_profiles");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, id + ".json");
	}

}
