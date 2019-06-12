package app.editors.epd.contents;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.M;
import app.util.UI;

class ContentDialog extends FormDialog {

	static int open(Object obj) {
		ContentDialog cd = new ContentDialog();
		return cd.open();
	}

	ContentDialog() {
		super(UI.shell());
	}

	@Override
	protected void createFormContent(IManagedForm mform) {

		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(mform.getForm(), tk);

		Composite comp = UI.formComposite(body, tk);
		UI.gridData(comp, true, false);
		UI.formText(comp, tk, M.Name);
		UI.formText(comp, tk, "weight-%");
		UI.formText(comp, tk, "Mass [kg]");
		UI.formText(comp, tk, "CAS No");
		UI.formText(comp, tk, "EC No");
		UI.formText(comp, tk, "Renewable resource");
		UI.formText(comp, tk, "Post cosumer material recycled content");
		UI.formText(comp, tk, "Material recyclable content");
		UI.formMultiText(comp, tk, "Comment");
	}

}
