package app.store;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.lists.Location;
import org.openlca.ilcd.lists.LocationList;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.io.conversion.ProcessExtensions;
import epd.model.EpdDataSet;
import epd.model.MaterialProperty;

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

	public static void saveMaterialProperties(List<MaterialProperty> list) {
		MaterialProperties.save(list);
	}

	public static List<MaterialProperty> getMaterialProperties() {
		return MaterialProperties.get();
	}

}
