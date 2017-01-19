package app.store;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.model.IndicatorMapping;

public final class IndicatorMappings {

	private static final String NAME = "indicator_mappings.json";

	private IndicatorMappings() {
	}

	public static List<IndicatorMapping> get() {
		List<IndicatorMapping> list = fromFile();
		if (list != null)
			return list;
		try (InputStream is = IndicatorMappings.class
				.getResourceAsStream(NAME)) {
			IndicatorMapping[] mappings = Json.read(is,
					IndicatorMapping[].class);
			return asList(mappings);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(IndicatorMappings.class);
			log.error("failed to read " + NAME, e);
			return new ArrayList<>();
		}
	}

	static void save(List<IndicatorMapping> mappings) {
		File file = file();
		Json.write(mappings, file);
	}

	private static List<IndicatorMapping> fromFile() {
		File file = file();
		if (file == null || !file.exists()) {
			return null;
		}
		IndicatorMapping[] mappings = Json.read(file, IndicatorMapping[].class);
		return mappings == null ? null : asList(mappings);
	}

	private static List<IndicatorMapping> asList(IndicatorMapping[] mappings) {
		ArrayList<IndicatorMapping> list = new ArrayList<>();
		if (mappings == null)
			return list;
		for (IndicatorMapping im : mappings)
			list.add(im);
		return list;
	}

	private static File file() {
		File dir = App.store.getRootFolder();
		return new File(dir, NAME);
	}
}
