package app.editors.matprops;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.Editors;
import app.editors.IEditor;
import app.editors.SimpleEditorInput;
import app.store.Store;
import epd.model.MaterialProperty;

public class MaterialPropertyEditor extends FormEditor implements IEditor {

	private static final String ID = "material.properties.editor";

	private Logger log = LoggerFactory.getLogger(getClass());

	private List<MaterialProperty> properties;
	private boolean dirty;

	public static void open() {
		Editors.open(new SimpleEditorInput("Material properties"), ID);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		properties = Store.getMaterialProperties();
	}

	@Override
	public void setDirty() {
		if (!dirty) {
			dirty = true;
			editorDirtyStateChanged();
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page(this, properties));
		} catch (Exception e) {
			log.error("failed to add editor page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		Store.saveMaterialProperties(properties);
		dirty = false;
		editorDirtyStateChanged();
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
