package app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.openlca.ilcd.io.FileStore;
import org.slf4j.LoggerFactory;

import epd.index.Index;

/**
 * The workspace is the folder where the EPD editor stores its data.
 */
public class Workspace {

	/**
	 * The root folder of the workspace.
	 */
	public final File folder;

	/**
	 * The file store where the ILCD data sets are saved.
	 */
	public final FileStore store;

	/**
	 * The data set index of the file store.
	 */
	private volatile Index index;

	private Workspace(File folder) {
		this.folder = folder;
		store = new FileStore(folder);
	}

	synchronized void updateIndex(Index index) {
		saveIndex();
		this.index = index;
	}

	public Index index() {
		var idx = index;
		if (idx != null)
			return idx;
		synchronized (this) {
			if (index == null) {
				index = Index.load(new File(folder, "index.json"));
			}
			return index;
		}
	}

	/**
	 * Opens a workspace in the given folder.
	 */
	public static Workspace open(File folder) {
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (Exception e) {
				throw new RuntimeException(
					"failed to open workspace @" + folder, e);
			}
		}
		return new Workspace(folder);
	}

	/**
	 * Opens a workspace in the default folder of the EPD editor which is the
	 * ~/.epd-editor folder in the users directory.
	 */
	public static Workspace openDefault() {
		var home = System.getProperty("user.home");
		var dir = new File(new File(home), ".epd-editor");
		return open(dir);
	}

	public synchronized void saveIndex() {
		index.dump(new File(folder, "index.json"));
	}

	/**
	 * Get the version with which the workspace is tagged. This version can be
	 * different to the version of the application when the version is not in
	 * sync with the application yet.
	 */
	public String version() {
		var versionFile = Paths.get(folder.getAbsolutePath(), ".version");
		if (!Files.isRegularFile(versionFile))
			return "0";
		try {
			var bytes = Files.readAllBytes(versionFile);
			var version = new String(bytes, StandardCharsets.UTF_8);
			return version.trim();
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(App.class);
			log.error("failed to read workspace version from " + versionFile, e);
			return "0";
		}
	}

	/**
	 * Sets the version tag of this workspace. The version is stored in a
	 * {@code .version} file in the workspace.
	 *
	 * @param version the new version of the workspace
	 */
	public void setVersion(String version) {
		var v = version == null ? "0" : version;
		var versionFile = Paths.get(folder.getAbsolutePath(), ".version");
		try {
			Files.writeString(versionFile, v);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(App.class);
			log.error("failed to write workspace version to " + versionFile, e);
		}
	}

	public void syncFilesFrom(File dir) {
		var log = LoggerFactory.getLogger(getClass());
		log.info("sync folder {} with workspace {}", dir, this.folder);
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			log.warn("folder {} does not exist", dir);
			return;
		}
		if (dir.equals(this.folder)) {
			log.warn("cannot sync folder with itself");
			return;
		}
		try {
			FileSync.sync(dir.toPath(), this.folder.toPath());
		} catch (Exception e) {
			log.error("failed to init data folder @" + this.folder, e);
		}
	}

	@Override
	public String toString() {
		return "Workspace{" + "folder=" + folder + '}';
	}

	/**
	 * Copies all files from a source directory to a target directory. Files that
	 * exists in both directories will be overwritten by the version in the source
	 * directory.
	 */
	private static class FileSync extends SimpleFileVisitor<Path> {

		private final Path sourceDir;
		private final Path targetDir;

		private FileSync(Path sourceDir, Path targetDir) {
			this.sourceDir = sourceDir;
			this.targetDir = targetDir;
		}

		static void sync(Path sourceDir, Path targetDir) throws IOException {
			if (!Files.exists(sourceDir))
				return;
			if (!Files.exists(targetDir)) {
				Files.createDirectories(targetDir);
			}
			var sync = new FileSync(sourceDir, targetDir);
			Files.walkFileTree(sourceDir, sync);
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
			var target = targetDir.resolve(sourceDir.relativize(dir));
			if (!Files.exists(target)) {
				Files.createDirectories(target);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
			var target = targetDir.resolve(sourceDir.relativize(file));

			// do not overwrite user settings
			if (target.getFileName().toString().equals("settings.json")
				&& Files.exists(target)) {
				return FileVisitResult.CONTINUE;
			}

			Files.copy(file, target,
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES);
			return FileVisitResult.CONTINUE;
		}
	}
}
