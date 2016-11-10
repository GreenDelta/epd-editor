package app.editors.contact;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.Contact;

import app.App;
import app.util.UI;

public class ContactPage extends FormPage {

	private final Contact contact;
	private FormToolkit tk;

	public ContactPage(ContactEditor editor) {
		super(editor, "ContactPage", "#Contact Data Set");
		contact = editor.contact;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform,
				"#Contact: " + App.s(contact.getName()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite comp = UI.formSection(body, tk, "#Contact information");
		Text t = UI.formText(comp, tk, "#Short name");
		List<LangString> shortName = contact.contactInfo.dataSetInfo.shortName;
		t.setText(App.s(shortName));
		t.addModifyListener(e -> {
			LangString.set(shortName, t.getText(), App.lang);
		});

	}

}
