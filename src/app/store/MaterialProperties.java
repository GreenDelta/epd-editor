package app.store;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.model.MaterialProperty;

class MaterialProperties {

	private static final String NAME = "material_properties.json";

	static List<MaterialProperty> get() {
		List<MaterialProperty> list = fromFile();
		if (list != null)
			return list;
		try (InputStream is = MaterialProperties.class
				.getResourceAsStream(NAME)) {
			MaterialProperty[] props = Json.read(is, MaterialProperty[].class);
			return asList(props);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(MaterialProperties.class);
			log.error("failed to read " + NAME, e);
			return new ArrayList<>();
		}
	}

	static void save(List<MaterialProperty> properties) {
		File file = file();
		Json.write(properties, file);
	}

	private static List<MaterialProperty> fromFile() {
		File file = file();
		if (file == null || !file.exists()) {
			return null;
		}
		MaterialProperty[] props = Json.read(file, MaterialProperty[].class);
		return props == null ? null : asList(props);
	}

	private static List<MaterialProperty> asList(MaterialProperty[] props) {
		ArrayList<MaterialProperty> list = new ArrayList<>();
		if (props == null)
			return list;
		for (MaterialProperty prop : props)
			list.add(prop);
		return list;
	}

	private static File file() {
		File dir = App.store.getRootFolder();
		return new File(dir, NAME);
	}

}
