package app.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;

public class Editors {

	public static void open(IEditorInput input, String editorId) {
		new OpenInUIJob(input, editorId).schedule();
	}

	private static IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage();
	}

	private static class OpenInUIJob extends UIJob {

		private IEditorInput input;
		private String editorId;

		public OpenInUIJob(IEditorInput input, String editorId) {
			super(M.OpenEditor);
			this.input = input;
			this.editorId = editorId;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				getActivePage().openEditor(input, editorId);
				return Status.OK_STATUS;
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("Open editor " + editorId + " failed.", e);
				return Status.CANCEL_STATUS;
			}
		}
	}
}
