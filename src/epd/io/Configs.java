package epd.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import epd.model.MaterialProperty;

public final class Configs {

	private static final String MAPPING_CONFIG = "mapping_config.json";
	private static final String SERVER_CONFIG = "server_config.json";

	private Configs() {
	}

	public static MappingConfig getDefaultMappingConfig() {
		return getDefault(MAPPING_CONFIG, MappingConfig.class);
	}

	private static <T> T getDefault(String fileName, Class<T> clazz) {
		InputStream stream = Configs.class.getResourceAsStream(fileName);
		return get(stream, clazz);
	}

	private static <T> T get(File file, Class<T> clazz) {
		try (FileInputStream fis = new FileInputStream(file)) {
			return get(fis, clazz);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Configs.class);
			log.error("failed to read config " + clazz, e);
			return null;
		}
	}

	public static MappingConfig getMappingConfig(InputStream stream) {
		return get(stream, MappingConfig.class);
	}

	public static List<MaterialProperty> getMaterialProperties(
			InputStream stream) {
		MaterialProperty[] properties = get(stream, MaterialProperty[].class);
		return new ArrayList<>(Arrays.asList(properties));
	}

	private static <T> T get(InputStream stream, Class<T> clazz) {
		try (InputStreamReader reader = new InputStreamReader(stream,
				"utf-8")) {
			Gson gson = new Gson();
			return gson.fromJson(reader, clazz);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Configs.class);
			log.error("failed to read config " + clazz, e);
			return null;
		}
	}

	public static MappingConfig getMappingConfig(File workspace) {
		if (workspace == null || !workspace.isDirectory())
			return getDefaultMappingConfig();
		File file = new File(workspace, MAPPING_CONFIG);
		if (!file.exists())
			return getDefaultMappingConfig();
		else
			return get(file, MappingConfig.class);
	}

	public static void save(Object config, File file) {
		try (FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fos,
						"utf-8");
				BufferedWriter buffer = new BufferedWriter(writer)) {
			Gson gson = new Gson();
			String string = gson.toJson(config);
			buffer.write(string);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Configs.class);
			log.error("failed to write config " + config + " to " + file, e);
		}
	}

	public static void save(MappingConfig config, EpdStore store) {
		File file = new File(store.baseDir, MAPPING_CONFIG);
		save(config, file);
	}

}
