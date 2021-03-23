package app;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
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
	public final Index index;

	private Workspace(File folder) {
		this.folder = folder;
		store = new FileStore(folder);
		// copy the default data into the workspace if there
		// is a "data" folder in the execution folder
		syncWith(new File("data"));
		// load the index after we synced the files
		index = Index.load(new File(folder, "index.json"));
	}

	private Workspace(Workspace ws, Index index) {
		this.folder = ws.folder;
		this.store = new FileStore(ws.folder);
		this.index = index;
	}

	Workspace updateIndex(Index index) {
		var ws = new Workspace(this, index);
		ws.saveIndex();
		return ws;
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

	@Override
	public String toString() {
		return "Workspace{" + "folder=" + folder + '}';
	}

	public void syncWith(File folder) {
		if (!folder.exists()
				|| !folder.isDirectory()
				|| folder.equals(this.folder)) {
			var log = LoggerFactory.getLogger(getClass());
			log.info("sync folder '{}' does not exist",
					folder.getAbsolutePath());
			return;
		}
		var files = folder.listFiles();
		if (files == null)
			return;
		try {
			for (var f : files) {
				if (f.isFile()) {
					FileUtils.copyFileToDirectory(f, this.folder);
				} else {
					FileUtils.copyDirectoryToDirectory(f, this.folder);
				}
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(App.class);
			log.error("failed to init data folder @" + this.folder, e);
		}
	}
}
