package app;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.io.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.index.Index;
import epd.util.Strings;

public class App {

	private static AppSettings settings;

	public static FileStore store;
	public static File workspace;
	public static Index index;

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
			index = Index.load(new File(dir, "index.json"));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(App.class);
			log.error("failed to init App", e);
		}
	}

	public static AppSettings settings() {
		if (settings == null)
			settings = AppSettings.load();
		return settings;
	}

	public static String lang() {
		return settings().lang;
	}

	public static void dumpIndex() {
		Logger log = LoggerFactory.getLogger(App.class);
		log.info("update index file");
		index.dump(new File(workspace, "index.json"));
	}

	public static String s(List<LangString> strings) {
		if (strings == null)
			return "";
		String s = LangString.getVal(strings, lang());
		return s == null ? "" : s;
	}

	public static String header(List<LangString> strings, int length) {
		String s = LangString.getFirst(strings, lang());
		return s == null ? "" : Strings.cut(s, length);
	}

	public static Job runInUI(String name, Runnable runnable) {
		WrappedUIJob job = new WrappedUIJob(name, runnable);
		job.setUser(true);
		job.schedule();
		return job;
	}

	/**
	 * Wraps a runnable in a job and executes it using the Eclipse jobs
	 * framework. No UI access is allowed for the runnable.
	 */
	public static Job run(String name, Runnable runnable) {
		return run(name, runnable, null);
	}

	/**
	 * See {@link App#run(String, Runnable)}. Additionally, this method allows
	 * to give a callback which is executed in the UI thread when the runnable
	 * is finished.
	 */
	public static Job run(String name, Runnable runnable, Runnable callback) {
		WrappedJob job = new WrappedJob(name, runnable);
		if (callback != null)
			job.setCallback(callback);
		job.setUser(true);
		job.schedule();
		return job;
	}

	public static void runWithProgress(String name,
			Consumer<IProgressMonitor> fn) {
		try {
			IProgressService progress = PlatformUI.getWorkbench()
					.getProgressService();
			progress.run(true, false, monitor -> {
				fn.accept(monitor);
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(App.class);
			log.error("Error while running progress " + name, e);
		}
	}

}
