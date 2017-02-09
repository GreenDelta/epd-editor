package app.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.forms.editor.FormEditor;

public abstract class BaseEditor extends FormEditor implements IEditor {

	protected boolean dirty;
	protected List<Runnable> saveHandlers = new ArrayList<>();

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

	public void onSaved(Runnable handler) {
		saveHandlers.add(handler);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void setPartName(String partName) {
		super.setPartName(partName);
	}
}
