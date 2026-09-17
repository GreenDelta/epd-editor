package app.store;

import java.util.UUID;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.NaviSync;
import epd.model.Version;
import epd.model.Xml;

public final class Data {

	private Data() {
	}

	/// Saves the given data set in the local data store and updates the data set
	/// index. Returns an error when this failed; the error is also written to
	/// the log.
	public static Res<Void> save(IDataSet ds) {
		if (ds == null)
			return Res.error("No data set given");
		try {
			Ref ref = Ref.of(ds);
			var workspace = App.getWorkspace();
			workspace.store.put(ds);

			var index = workspace.index();
			index.remove(ref);
			index.add(ds);
			workspace.saveIndex();

			RefTrees.cache(ds);
			new NaviSync(workspace.index()).run();
			return Res.ok();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to save data set {}", ds, e);
			return Res.error("Failed to save data set", e);
		}
	}

	public static void delete(Ref ref) {
		if (ref == null)
			return;
		try {
			var workspace = App.getWorkspace();
			workspace.store.delete(ref.getDataSetClass(), ref.getUUID());
			workspace.index().remove(ref);
			workspace.saveIndex();
			new NaviSync(workspace.index()).run();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to delete data set {}", ref, e);
		}
	}

	public static IDataSet load(Ref ref) {
		if (ref == null || !ref.isValid())
			return null;
		try {
			var store = App.getWorkspace().store;
			return store.get(ref.getDataSetClass(), ref.getUUID());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to load data set {}", ref, e);
			return null;
		}
	}

	public static void updateVersion(IDataSet ds) {
		if (ds == null)
			return;
		var v = Version.fromString(DataSets.getVersion(ds))
			.incUpdate()
			.toString();
		DataSets.withVersion(ds, v);
		DataSets.withTimeStamp(ds, Xml.now());
	}

	/// Assigns a new identity to the given data set: a new UUID, a version
	/// reset to `00.00.000`, the current time stamp, and the given name. This
	/// is used when a data set is duplicated or saved as a copy.
	public static void assignNewIdentity(IDataSet ds, String name) {
		if (ds == null)
			return;
		DataSets.withUUID(ds, UUID.randomUUID().toString());
		DataSets.withVersion(ds, Version.asString(0));
		DataSets.withTimeStamp(ds, Xml.now());
		if (Strings.isNotBlank(name)) {
			DataSets.withBaseName(ds, LangString.of(name.strip(), App.lang()));
		}
	}
}
