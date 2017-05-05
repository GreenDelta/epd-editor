package app.store.validation;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;

public final class ValidationProfiles {

	private ValidationProfiles() {
	}

	public static List<URL> getURLs() {
		File dir = dir();
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return Collections.emptyList();
		List<URL> urls = new ArrayList<>();
		for (File file : dir.listFiles()) {
			URL url = getURL(file);
			if (url != null)
				urls.add(url);
		}
		return urls;
	}

	private static URL getURL(File file) {
		if (file == null || !file.isFile() || !file.exists()
				|| !file.getName().endsWith(".jar"))
			return null;
		try {
			return file.toURI().toURL();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ValidationProfiles.class);
			log.error("failed to get file URL for " + file, e);
			return null;
		}
	}

	private static File dir() {
		return new File(App.workspace, "validation_profiles");
	}

}
