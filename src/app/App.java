package app;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.openlca.ilcd.commons.LangString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.util.Strings;

public class App {

	private static AppSettings _settings;
	private static Workspace _workspace;

	/**
	 * Initializes the workspace and resources of the RCP application.
	 */
	public static void initRCP() {
		try {
			_workspace = Workspace.openDefault();
			Platform.getInstanceLocation().release();
			var url = new URL("file", null,
				_workspace.folder.getAbsolutePath());
			Platform.getInstanceLocation().set(url, true);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(App.class);
			log.error("failed to init App", e);
		}
	}

	public static Workspace getWorkspace() {
		if (_workspace == null) {
			_workspace = Workspace.openDefault();
		}
		return _workspace;
	}

	public static AppSettings settings() {
		if (_settings == null) {
			_settings = AppSettings.load(getWorkspace());
		}
		return _settings;
	}

	public static String lang() {
		return settings().lang;
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

	public static void run(IRunnableWithProgress p) {
		run(p, null);
	}

	public static void run(IRunnableWithProgress p, Runnable uiFn) {
		if (p == null)
			return;
		IProgressService progress = PlatformUI.getWorkbench()
			.getProgressService();
		try {
			progress.run(true, true, p);
			if (uiFn != null) {
				new WrappedUIJob("Update UI ...", uiFn).schedule();
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(App.class);
			log.error("failed to run " + p.getClass(), e);
		}
	}
}
