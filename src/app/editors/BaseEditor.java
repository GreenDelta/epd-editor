package app.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.forms.editor.FormEditor;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.DataSets;

import app.App;
import app.M;
import app.store.Data;
import app.util.UI;

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

	/// Asks for a new name, assigns a new identity to the given copy of the
	/// data set that is edited in this editor, saves it, and opens it in a new
	/// editor. Nothing is done when the given copy is `null`.
	protected void saveAs(IDataSet copy) {
		if (copy == null)
			return;
		var d = new InputDialog(UI.shell(), M.SaveAs,
			M.SaveAs_Message + ": ",
			App.s(DataSets.getBaseName(copy)), null);
		if (d.open() != Window.OK || Strings.isBlank(d.getValue()))
			return;
		Data.assignNewIdentity(copy, d.getValue());
		Data.save(copy);
		Editors.open(Ref.of(copy));
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
