package app.editors.methods;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.methods.ImpactMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.RefCheck;
import app.editors.RefEditorInput;
import app.store.Data;

public class MethodEditor extends BaseEditor {

	private static final String ID = "method.editor";

	public ImpactMethod method;

	public static void open(Ref ref) {
		if (ref == null)
			return;
		RefEditorInput input = new RefEditorInput(ref);
		Editors.open(input, ID);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		Editors.setTabTitle(input, this);
		try {
			RefEditorInput in = (RefEditorInput) input;
			method = App.store().get(ImpactMethod.class, in.ref().getUUID());
			RefCheck.on(method);
		} catch (Exception e) {
			throw new PartInitException("Failed to open method editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			Editors.addInfoPages(this, method);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			Data.updateVersion(method);
			Data.save(method);
			for (Runnable handler : saveHandlers) {
				handler.run();
			}
			dirty = false;
			editorDirtyStateChanged();
			Editors.setTabTitle(method, this);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save LCIA data set", e);
		}
	}

}
