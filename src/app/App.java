package app;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.openlca.ilcd.io.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	public static String lang;
	public static FileStore store;
	public static File workspace;

	public static void init() {
		try {
			File dir = new File("data");
			if (!dir.exists())
				dir.mkdirs();
			Platform.getInstanceLocation().release();
			URL workspaceUrl = new URL("file", null, dir.getAbsolutePath());
			Platform.getInstanceLocation().set(workspaceUrl, true);
			workspace = dir;
			store = new FileStore(dir);
			lang = "en";
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(App.class);
			log.error("failed to init App", e);
		}
	}
}
