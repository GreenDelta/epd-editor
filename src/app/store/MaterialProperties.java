package app.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.commons.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import epd.model.MaterialProperty;
import epd.model.MaterialPropertyValue;
import epd.util.Json;

public class MaterialProperties {

	private static final String NAME = "material_properties.json";

	public static List<MaterialProperty> get() {
		List<MaterialProperty> list = fromFile();
		if (list != null)
			return list;
		var stream = MaterialProperties.class.getResourceAsStream(NAME);
		if (stream == null)
			return new ArrayList<>();
		try (stream) {
			MaterialProperty[] props = Json.read(stream, MaterialProperty[].class);
			return asList(props);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(MaterialProperties.class);
			log.error("failed to read " + NAME, e);
			return new ArrayList<>();
		}
	}

	/// Returns the material properties of the store that are not used in the
	/// given values yet, compared by ID and sorted by name. This is used to make
	/// sure that a material property is not added twice to a flow.
	public static List<MaterialProperty> getUnused(
		List<MaterialPropertyValue> used
	) {
		var usedIds = used == null
			? Set.<String>of()
			: used.stream()
				.filter(v -> v != null && v.property != null && v.property.id != null)
				.map(v -> v.property.id)
				.collect(Collectors.toSet());
		var unused = new ArrayList<MaterialProperty>();
		for (var prop : get()) {
			if (prop == null || prop.id == null || usedIds.contains(prop.id))
				continue;
			unused.add(prop);
		}
		unused.sort((p1, p2) -> Strings.compareIgnoreCase(p1.name, p2.name));
		return unused;
	}

	public static void save(List<MaterialProperty> properties) {
		File file = file();
		Json.write(properties, file);
	}

	private static List<MaterialProperty> fromFile() {
		File file = file();
		if (!file.exists()) {
			return null;
		}
		var props = Json.read(file, MaterialProperty[].class);
		return props == null ? null : asList(props);
	}

	private static List<MaterialProperty> asList(MaterialProperty[] props) {
		ArrayList<MaterialProperty> list = new ArrayList<>();
		if (props == null)
			return list;
		list.addAll(Arrays.asList(props));
		return list;
	}

	private static File file() {
		File dir = App.store().getRootFolder();
		return new File(dir, NAME);
	}

}
