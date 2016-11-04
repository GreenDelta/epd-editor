package app;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.io.Configs;
import epd.io.MappingConfig;
import epd.io.conversion.Converter;
import epd.model.EpdDataSet;

public class Store {

	public static EpdDataSet openEPD(Ref ref) {
		Logger log = LoggerFactory.getLogger(Store.class);
		try {
			log.trace("open EPD data set {}", ref);
			Process process = App.store.get(Process.class, ref.uuid);
			MappingConfig config = Configs.getMappingConfig(App.workspace);
			String[] langs = new String[] { App.lang };
			EpdDataSet dataSet = Converter.convert(process, config, langs);
			Converter.readProductData(dataSet, App.store);
			return dataSet;
		} catch (Exception e) {
			log.error("failed to open EPD data set " + ref, e);
			return null;
		}
	}

	public static Process saveEPD(EpdDataSet dataSet) {
		Logger log = LoggerFactory.getLogger(Store.class);
		try {
			log.trace("update EPD data set {}", dataSet);
			MappingConfig config = Configs.getMappingConfig(App.workspace);
			Process process = Converter.convert(dataSet, config);
			App.store.put(process, process.processInfo.dataSetInfo.uuid);
			Converter.writeProductData(dataSet, App.store);
			return process;
		} catch (Exception e) {
			log.error("failed to save EPD data set " + dataSet, e);
			return null;
		}
	}

}
