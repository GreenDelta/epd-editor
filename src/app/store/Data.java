package app.store;

import org.openlca.ilcd.commons.IDataSet;
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

	public static void save(IDataSet ds) {
		if (ds == null)
			return;
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
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("Failed to update data set: " + ds, e);
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
			log.error("failed to delete data set " + ref, e);
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
			log.error("failed to load data set " + ref, e);
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
}
