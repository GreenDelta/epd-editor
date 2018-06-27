package app.store;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Flows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import app.App;
import epd.io.conversion.Vocab;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.Indicator.Type;
import epd.model.RefStatus;
import epd.util.Strings;

public final class EpdProfiles {

	private static final String DEFAULT = "EN_15804";

	private static Map<String, EpdProfile> cache = new HashMap<>();

	private EpdProfiles() {
	}

	public static void evictCache() {
		cache.clear();
	}

	public static boolean isDefault(EpdProfile p) {
		if (p == null || p.id == null)
			return false;
		String settingsID = App.settings().profile;
		if (settingsID == null)
			return p.id.equals(DEFAULT);
		return p.id.equals(settingsID);
	}

	/** Get the active profile of the application. */
	public static EpdProfile getDefault() {
		String id = App.settings().profile;
		if (id == null) {
			id = DEFAULT;
		}
		EpdProfile p = get(id);
		if (p != null)
			return p;

		// id != DEFAULT and does not exist -> switch to default
		if (!id.equals(DEFAULT)) {
			App.settings().profile = DEFAULT;
			App.settings().save();
			p = get(DEFAULT);
			if (p != null)
				return p;
		}

		// no DEFAULT profile in storage -> extract it
		InputStream stream = EpdProfiles.class
				.getResourceAsStream(DEFAULT + ".json");
		p = Json.read(stream, EpdProfile.class);
		save(p);

		if (p == null) {
			// this should never happen, but who knows
			p = new EpdProfile();
			p.id = DEFAULT;
			p.name = DEFAULT;
			Logger log = LoggerFactory.getLogger(EpdProfiles.class);
			log.error("failed to load an EPD profile; even the default");
		}
		return p;
	}

	public static EpdProfile get(String id) {
		if (id == null)
			return null;
		EpdProfile p = cache.get(id);
		if (p != null)
			return p;
		File f = file(id);
		if (f == null || !f.exists())
			return null;
		p = Json.read(f, EpdProfile.class);
		sync(p);
		cache.put(id, p);
		return p;
	}

	public static EpdProfile get(Process p) {
		if (p == null)
			return getDefault();
		String profileID = p.otherAttributes.get(Vocab.PROFILE_ATTR);
		EpdProfile profile = EpdProfiles.get(profileID);
		return profile != null ? profile : getDefault();
	}

	/** Get all EPD profiles from the workspace. */
	public static List<EpdProfile> getAll() {
		getDefault(); // make sure that the default profile is loaded.
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

	/**
	 * Save the given profile in the workspace. The profile is synchronized
	 * before it is saved.
	 */
	public static void save(EpdProfile profile) {
		if (profile == null || profile.id == null)
			return;
		sync(profile);
		File file = file(profile.id);
		Json.write(profile, file);
		cache.put(profile.id, profile);
	}

	/** Delete the profile with the given ID. */
	public static void delete(String id) {
		File file = file(id);
		if (file == null || !file.exists())
			return;
		file.delete();
		cache.remove(id);
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
		if (i == null)
			return;
		if (i.type == Type.LCI) {
			stats.add(syncFlow(i));
		} else {
			stats.add(syncMethod(i));
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

	private static RefStatus syncMethod(Indicator i) throws Exception {
		LCIAMethod m = App.store.get(LCIAMethod.class, i.uuid);
		if (m == null) {
			return RefStatus.error(i.getRef(App.lang()),
					"Not found in local store");
		} else {
			i.name = App.s(m.getName());
			return RefStatus.info(i.getRef(App.lang()), "Updated");
		}
	}

	private static RefStatus syncFlow(Indicator i) throws Exception {
		Flow flow = App.store.get(Flow.class, i.uuid);
		if (flow == null) {
			return RefStatus.error(i.getRef(App.lang()),
					"Not found in local store");
		} else {
			FlowName flowName = Flows.getFlowName(flow);
			if (flowName != null) {
				i.name = App.s(flowName.baseName);
			}
			return RefStatus.info(i.getRef(App.lang()), "Updated");
		}
	}

	/**
	 * Download all EPD-Profiles from the given base URL. EPD-Profiles are
	 * located under the /resource/profiles path of a soda4LCA server. These
	 * profiles are stored/updated locally. The given callback function is
	 * called for each EPD profile that was downloaded.
	 */
	public static void downloadAll(String baseURL, Consumer<EpdProfile> fn) {
		if (baseURL == null)
			return;
		Logger log = LoggerFactory.getLogger(EpdProfiles.class);
		String url = baseURL.split("/resource")[0] + "/resource/profiles/";
		log.info("Try to download profiles from {}", url);
		try {
			URLConnection con = new URL(url).openConnection();
			if (!(con instanceof HttpURLConnection)) {
				log.warn("No HTTP connection");
				return;
			}
			HttpURLConnection http = (HttpURLConnection) con;
			http.setRequestMethod("GET");
			http.connect();
			if (http.getResponseCode() >= 400) {
				log.warn("Response code = {}", http.getResponseCode());
				return;
			}
			try (InputStream in = con.getInputStream();
					Reader reader = new InputStreamReader(in, "utf-8")) {
				JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
				for (JsonElement elem : obj.getAsJsonArray("profiles")) {
					if (!elem.isJsonObject())
						continue;
					JsonElement idElem = elem.getAsJsonObject().get("id");
					if (idElem == null)
						continue;
					String id = idElem.getAsString();
					String profileUrl = url + id;
					EpdProfile p = download(profileUrl);
					if (p != null && fn != null) {
						fn.accept(p);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to download profiles", e);
		}
	}

	/**
	 * Download an EPD profile and store it locally. Returns null if the
	 * download failed. Details are written to the log.
	 */
	public static EpdProfile download(String url) {
		if (url == null)
			return null;
		Logger log = LoggerFactory.getLogger(EpdProfiles.class);
		log.info("Download profile from {}", url);
		try {
			URLConnection con = new URL(url).openConnection();
			if (!(con instanceof HttpURLConnection)) {
				log.warn("No HTTP connection");
				return null;
			}
			HttpURLConnection http = (HttpURLConnection) con;
			http.setRequestMethod("GET");
			http.connect();
			if (http.getResponseCode() >= 400) {
				log.warn("Response code = {}", http.getResponseCode());
				return null;
			}
			try (InputStream in = con.getInputStream()) {
				EpdProfile p = Json.read(in, EpdProfile.class);
				if (p == null || p.id == null) {
					log.warn("Could not read profile");
					return null;
				}
				save(p);
				log.info("Saved profile {}", p.id);
				return p;
			}
		} catch (Exception e) {
			log.error("Failed to download profiles", e);
			return null;
		}
	}
}
