package app.editors.flow;

import org.eclipse.ui.forms.FormDialog;
import org.openlca.ilcd.flows.Flow;

import app.util.UI;

/**
 * When a flow property is added to a flow, we open this dialog when we have
 * some suggestions for additional flow and material properties that the user
 * may want to add.
 */
class PropertyDepsDialog extends FormDialog {

	PropertyDepsDialog() {
		super(UI.shell());
	}

	static boolean add(Flow flow) {
		return true;
	}

}
