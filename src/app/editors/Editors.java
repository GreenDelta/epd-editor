package app.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.editors.contact.ContactEditor;
import app.editors.epd.EpdEditor;
import app.editors.flow.FlowEditor;
import app.editors.flowproperty.FlowPropertyEditor;
import app.editors.methods.MethodEditor;
import app.editors.source.SourceEditor;
import app.editors.unitgroup.UnitGroupEditor;

public class Editors {

	public static void open(Ref ref) {
		if (ref == null || ref.type == null || ref.uuid == null)
			return;
		switch (ref.type) {
		case PROCESS:
			EpdEditor.open(ref);
			break;
		case FLOW:
			FlowEditor.open(ref);
			break;
		case CONTACT:
			ContactEditor.open(ref);
			break;
		case SOURCE:
			SourceEditor.open(ref);
			break;
		case FLOW_PROPERTY:
			FlowPropertyEditor.open(ref);
			break;
		case UNIT_GROUP:
			UnitGroupEditor.open(ref);
			break;
		case LCIA_METHOD:
			MethodEditor.open(ref);
			break;
		case EXTERNAL_FILE:
			break;
		}
	}

	public static void open(IEditorInput input, String editorId) {
		new OpenInUIJob(input, editorId).schedule();
	}

	@SuppressWarnings("unchecked")
	public static <T extends IEditorPart> T getActive() {
		try {
			return (T) getActivePage().getActiveEditor();
		} catch (ClassCastException e) {
			Logger log = LoggerFactory.getLogger(Editors.class);
			log.error("Error getting active editor", e);
			return null;
		}
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
