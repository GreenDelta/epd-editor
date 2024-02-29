package app.store;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.lists.CategorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import app.App;
import jakarta.xml.bind.JAXB;

public final class CategorySystems {

	private CategorySystems() {
	}

	public static CategorySystem get(File file) {
		if (file == null)
			return null;
		try {
			return JAXB.unmarshal(file, CategorySystem.class);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(CategorySystems.class);
			log.error("Failed to get category system from file " + file, e);
			return null;
		}
	}

	public static List<CategorySystem> get() {
		var list = new ArrayList<CategorySystem>();
		var files = getDir().listFiles();
		if (files == null)
			return list;
		for (var file : files) {
			try {
				var system = JAXB.unmarshal(file, CategorySystem.class);
				list.add(system);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(CategorySystems.class);
				log.error("failed to parse category file " + file, e);
			}
		}
		return list;
	}

	public static void put(CategorySystem system) {
		if (system == null)
			return;
		String name = system.getName();
		if (Strings.isNullOrEmpty(name))
			name = "unknown";
		try {
			name = URLEncoder.encode(name, StandardCharsets.UTF_8);
			File file = new File(getDir(), name + ".xml");
			JAXB.marshal(system, file);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(CategorySystems.class);
			log.error("failed to save category file " + name, e);
		}
	}

	private static File getDir() {
		var rootDir = App.store().getRootFolder();
		var dir = new File(rootDir, "classifications");
		if (!dir.exists()) {
			try {
				Files.createDirectories(dir.toPath());
			} catch (IOException e) {
				LoggerFactory.getLogger(CategorySystems.class)
					.error("failed to create classifications folder", e);
			}
		}
		return dir;
	}
}
