package app.editors;

import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.AppSettings;
import app.M;
import app.editors.connection.ConnectionEditor;
import app.editors.contact.ContactEditor;
import app.editors.epd.EpdEditor;
import app.editors.flow.FlowEditor;
import app.editors.flowproperty.FlowPropertyEditor;
import app.editors.methods.MethodEditor;
import app.editors.source.SourceEditor;
import app.editors.unitgroup.UnitGroupEditor;
import epd.util.Strings;

public class Editors {

	public static void setTabTitle(IEditorInput input, BaseEditor editor) {
		if (input == null || editor == null)
			return;
		String title = input.getName();
		editor.setPartName(Strings.cut(title, 75));
	}

	public static void setTabTitle(IDataSet ds, BaseEditor editor) {
		if (ds == null || editor == null)
			return;
		String title = App.s(DataSets.getBaseName(ds));
		editor.setPartName(Strings.cut(title, 75));
	}

	public static void open(Ref ref) {
		if (ref == null || ref.getType() == null || ref.getUUID() == null)
			return;
		switch (ref.getType()) {
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
		case IMPACT_METHOD:
			MethodEditor.open(ref);
			break;
		case MODEL:
		case EXTERNAL_FILE:
			// we have no editor for these things
			break;
		}
	}

	public static void open(IEditorInput input, String editorId) {
		new OpenInUIJob(input, editorId).schedule();
	}

	public static void close(Ref ref) {
		close(input -> {
			if (!(input instanceof RefEditorInput))
				return false;
			Ref editorRef = ((RefEditorInput) input).ref();
			return Objects.equals(ref, editorRef);
		});
	}

	public static void close(SodaConnection con) {
		close(input -> {
			if (!(input instanceof ConnectionEditor.Input))
				return false;
			SodaConnection editorCon = ((ConnectionEditor.Input) input).con;
			return Objects.equals(con, editorCon);
		});
	}

	public static void closeAll() {
		close(input -> true);
	}

	private static void close(Predicate<IEditorInput> fn) {
		try {
			IWorkbenchPage page = getActivePage();
			for (IEditorReference er : page.getEditorReferences()) {
				IEditorInput input = er.getEditorInput();
				if (fn.test(input)) {
					IEditorPart editor = er.getEditor(false);
					page.closeEditor(editor, false);
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Editors.class);
			log.error("Failed to close editors", e);
		}
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

	public static void addInfoPages(BaseEditor editor, IDataSet ds) {
		if (ds == null || editor == null)
			return;
		AppSettings settings = App.settings();
		try {
			if (settings.showDataSetXML)
				editor.addPage(new XmlPage(editor, ds));
			if (settings.showDataSetDependencies)
				editor.addPage(new DependencyPage(editor, ds));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Editors.class);
			log.error("Failed to add info pages", e);
		}
	}

	private static class OpenInUIJob extends UIJob {

		private final IEditorInput input;
		private final String editorId;

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
