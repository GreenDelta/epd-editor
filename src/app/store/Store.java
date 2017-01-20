package app.store;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.io.conversion.ProcessExtensions;
import epd.model.EpdDataSet;

public class Store {

	public static EpdDataSet openEPD(Ref ref) {
		Logger log = LoggerFactory.getLogger(Store.class);
		try {
			log.trace("open EPD data set {}", ref);
			Process process = App.store.get(Process.class, ref.uuid);
			EpdDataSet dataSet = ProcessExtensions.read(process,
					IndicatorMappings.get());
			return dataSet;
		} catch (Exception e) {
			log.error("failed to open EPD data set " + ref, e);
			return null;
		}
	}

	public static void saveEPD(EpdDataSet dataSet) {
		Logger log = LoggerFactory.getLogger(Store.class);
		try {
			log.trace("update EPD data set {}", dataSet);
			ProcessExtensions.write(dataSet, IndicatorMappings.get());
			Process process = dataSet.process;
			App.store.put(process);
		} catch (Exception e) {
			log.error("failed to save EPD data set " + dataSet, e);
		}
	}

}
