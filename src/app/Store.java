package app;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.lists.Location;
import org.openlca.ilcd.lists.LocationList;
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

	public static Set<Location> getLocations() {
		if (App.store == null)
			return Collections.emptySet();
		File folder = new File(App.store.getRootFolder(), "locations");
		if (!folder.exists())
			return Collections.emptySet();
		HashSet<Location> set = new HashSet<>();
		try {
			for (File file : folder.listFiles()) {
				LocationList list = JAXB.unmarshal(file, LocationList.class);
				for (Location loc : list.locations)
					set.add(loc);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Store.class);
			log.error("failed read location files from " + folder, e);
		}
		return set;
	}

}
