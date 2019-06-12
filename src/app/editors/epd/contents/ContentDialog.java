package app.editors.epd.contents;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.M;
import app.util.UI;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;

class ContentDialog extends FormDialog {

	private final ContentElement elem;

	static int open(ContentDeclaration decl, ContentElement elem) {
		if (elem == null)
			return CANCEL;
		ContentDialog cd = new ContentDialog(elem);
		int code = cd.open();
		if (code != OK)
			return code;
		// TODO: copy values

		Content.remove(decl, elem);
		// TODO: check parent
		decl.content.add(elem);
		return OK;
	}

	ContentDialog(ContentElement elem) {
		super(UI.shell());
		this.elem = elem;
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
