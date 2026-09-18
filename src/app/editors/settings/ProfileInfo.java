package app.editors.settings;

import java.io.File;
import java.util.jar.JarFile;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import com.okworx.ilcd.validation.profile.Profile;

@NullMarked
record ProfileInfo(File file, @Nullable Profile profile) {

	static ProfileInfo of(File file) {
		try (JarFile jar = new JarFile(file)) {
			var url = file.toURI().toURL();
			var manifest = jar.getManifest();
			var profile = new Profile(url);
			var atts = manifest.getAttributes("ILCD-Validator-Profile");
			if (atts != null) {
				profile.setName(atts.getValue("Profile-Name"));
				profile.setVersion(atts.getValue("Profile-Version"));
			}
			return new ProfileInfo(file, profile);
		} catch (Exception e) {
			LoggerFactory.getLogger(Process.class)
				.error("failed to load profile {}", file, e);
			return new ProfileInfo(file, null);
		}
	}

	String name() {
		return profile == null
			? file.getName()
			: profile.getName();
	}

	String version() {
		return profile == null
			? ""
			: profile.getVersion();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ProfileInfo other))
			return false;
		return file.equals(other.file);
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}
}
