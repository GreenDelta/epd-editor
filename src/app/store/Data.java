package app.store;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Sync;
import epd.io.conversion.FlowExtensions;
import epd.io.conversion.Extensions;
import epd.model.EpdDataSet;
import epd.model.EpdProduct;

public final class Data {

	private Data() {
	}

	public static EpdDataSet getEPD(Ref ref) {
		try {
			Process process = App.store.get(Process.class, ref.uuid);
			EpdDataSet dataSet = Extensions.read(process,
					IndicatorMappings.get());
			return dataSet;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to open EPD data set " + ref, e);
			return null;
		}
	}

	public static void save(EpdDataSet epd) {
		if (epd == null)
			return;
		try {
			Extensions.write(epd, IndicatorMappings.get());
			save(epd.process);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to save EPD data set " + epd, e);
		}
	}

	public static void save(EpdProduct product) {
		if (product == null)
			return;
		try {
			FlowExtensions.write(product);
			save(product.flow);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Data.class);
			log.error("failed to save flow " + product, e);
		}
	}

	public static void save(IDataSet ds) {
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
