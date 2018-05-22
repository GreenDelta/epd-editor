package app.store;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.Module;
import epd.model.RefStatus;
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
		get(); // make sure that the default profile is loaded.
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

	/** Delete the profile with the given ID. */
	public static void delete(String id) {
		File file = file(id);
		if (file == null || !file.exists())
			return;
		file.delete();
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

	/**
	 * Synchronizes the indicator and unit group references of the given profile
	 * with the data store of this application.
	 */
	public static List<RefStatus> sync(EpdProfile p) {
		if (p == null)
			return Collections.emptyList();
		List<RefStatus> stats = new ArrayList<>();
		for (Indicator i : p.indicators) {
			try {
				syncRefs(i, stats);
			} catch (Exception e) {
				stats.add(RefStatus.error(i.getRef(App.lang()),
						"Failed to lead refs.: " + e.getMessage()));
			}
		}
		return stats;
	}

	private static void syncRefs(Indicator i, List<RefStatus> stats)
			throws Exception {
		LCIAMethod m = App.store.get(LCIAMethod.class, i.uuid);
		if (m == null) {
			stats.add(RefStatus.error(i.getRef(App.lang()),
					"Not found in local store"));
		} else {
			i.name = App.s(m.getName());
			stats.add(RefStatus.info(i.getRef(App.lang()),
					"Updated"));
		}
		UnitGroup ug = App.store.get(UnitGroup.class, i.unitGroupUUID);
		if (ug == null) {
			stats.add(RefStatus.error(i.getUnitGroupRef(App.lang()),
					"Not found in local store"));
			return;
		}
		Ref uRef = Ref.of(ug);
		String unit = RefDeps.getRefUnit(ug);
		if (Strings.nullOrEmpty(unit)) {
			stats.add(RefStatus.error(uRef, "No reference unit found"));
			return;
		}
		i.unit = unit;
		stats.add(RefStatus.info(uRef, "Found"));
	}

}
