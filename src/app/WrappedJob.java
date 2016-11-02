package app;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WrappedJob extends Job {

	private Runnable runnable;
	private WrappedUIJob callback;

	public WrappedJob(String name, Runnable runnable) {
		super(name);
		this.runnable = runnable;
	}

	public void setCallback(Runnable callback) {
		if (callback == null)
			return;
		String name = "Callback of " + getName();
		this.callback = new WrappedUIJob(name, callback);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
		try {
			runnable.run();
			monitor.done();
			if (callback != null)
				callback.schedule();
			return Status.OK_STATUS;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to run " + getName(), e);
			return Status.CANCEL_STATUS;
		}
	}
}