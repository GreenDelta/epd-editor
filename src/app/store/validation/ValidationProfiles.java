package app.store.validation;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.okworx.ilcd.validation.profile.Profile;
import com.okworx.ilcd.validation.profile.ProfileManager;

import app.App;

public final class ValidationProfiles {

	private ValidationProfiles() {
	}

	public static List<File> getFiles() {
		File dir = dir();
		if (dir == null || !dir.exists())
			return Collections.emptyList();
		List<File> files = new ArrayList<>();
		for (File file : dir.listFiles()) {
			if (isJar(file))
				files.add(file);
		}
		return files;
	}

	public static File put(File file) {
		if (!isJar(file))
			return null;
		File dir = dir();
		if (!dir.exists())
			dir.mkdirs();
		File target = new File(dir, file.getName());
		try {
			Files.copy(file.toPath(), target.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			return target;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ValidationProfiles.class);
			log.error("failed to copy profile", e);
			return null;
		}
	}

	public static boolean contains(File file) {
		if (file == null)
			return false;
		return find(file.getName()) != null;
	}

	public static Profile getActive() {
		File file = find(App.settings().validationProfile);
		if (file == null)
			return null;
		try {
			URL url = file.toURI().toURL();
			return ProfileManager.getInstance().registerProfile(url);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ValidationProfiles.class);
			log.error("failed to load validation profile " + file, e);
			return null;
		}
	}

	private static File find(String fileName) {
		if (fileName == null)
			return null;
		for (File f : getFiles()) {
			if (f.getName().equalsIgnoreCase(fileName))
				return f;
		}
		return null;
	}

	private static File dir() {
		return new File(App.workspaceFolder(), "validation_profiles");
	}

	private static boolean isJar(File file) {
		if (file == null || !file.exists() || !file.isFile())
			return false;
		return file.getName().endsWith(".jar");
	}
}
