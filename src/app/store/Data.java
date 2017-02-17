package app.store;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Sync;

public final class Data {

	private Data() {
	}

	public static void update(IDataSet ds) {
		if (ds == null)
			return;
		try {
			Ref ref = Ref.of(ds);
			App.store.put(ds);
			App.index.remove(ref);
			App.index.add(ds);
			App.dumpIndex();
			new Sync(App.index).run();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("Failed to update data set: " + ds, e);
		}
	}

	public static void delete(Ref ref) {
		if (ref == null)
			return;
		try {
			App.store.delete(ref.getDataSetClass(), ref.uuid);
			App.index.remove(ref);
			App.dumpIndex();
			new Sync(App.index).run();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to delete data set " + ref, e);
		}
	}
}
