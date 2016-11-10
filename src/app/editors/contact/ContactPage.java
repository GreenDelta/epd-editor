package app.editors.contact;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInfo;

import app.App;
import app.editors.RefText;
import app.util.TextBuilder;
import app.util.UI;

public class ContactPage extends FormPage {

	private final Contact contact;
	private final ContactEditor editor;
	private FormToolkit tk;

	public ContactPage(ContactEditor editor) {
		super(editor, "ContactPage", "#Contact Data Set");
		this.editor = editor;
		contact = editor.contact;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform,
				"#Contact: " + App.s(contact.getName()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);

	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.formSection(body, tk, "#Contact information");
		DataSetInfo info = contact.contactInfo.dataSetInfo;
		tb.text(comp, "#Short name", info.shortName);
		tb.text(comp, "#Name", info.name);
		tb.text(comp, "#Address", info.contactAddress);
		tb.text(comp, "#Telephone", info.telephone, t -> info.telephone = t);
		tb.text(comp, "#Telefax", info.telefax, t -> info.telefax = t);
		tb.text(comp, "#WWW-Address", info.wwwAddress,
				t -> info.wwwAddress = t);
		UI.formLabel(comp, tk, "#Logo");
		RefText logo = new RefText(comp, tk, DataSetType.SOURCE);
		UI.gridData(logo, true, false);
		logo.setRef(info.logo);
		logo.onChange(ref -> {
			info.logo = ref;
			editor.setDirty();
		});
	}

}
