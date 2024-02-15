package app;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrappedUIJob extends UIJob {

	private final Runnable runnable;

	WrappedUIJob(String name, Runnable runnable) {
		super(name);
		this.runnable = runnable;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
		BusyIndicator.showWhile(getDisplay(), () -> {
			try {
				runnable.run();
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("UI callback failed", e);
			}
		});
		return Status.OK_STATUS;
	}
}
