package app.editors.contact;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInfo;

import app.App;
import app.editors.CategorySection;
import app.editors.RefText;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

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
		categorySection(body);
		adminSection(body);
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

	private void categorySection(Composite body) {
		DataSetInfo info = contact.contactInfo.dataSetInfo;
		CategorySection section = new CategorySection(editor,
				DataSetType.CONTACT, info.classifications);
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk,
				"#Administrative information");
		AdminInfo info = contact.adminInfo;
		Text timeT = UI.formText(comp, tk, "#Last change");
		timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		Text uuidT = UI.formText(comp, tk, "#UUID");
		if (contact.getUUID() != null)
			uuidT.setText(contact.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(contact.getVersion());
		vf.onChange(v -> info.publication.version = v);
		editor.onSaved(() -> {
			vf.setVersion(info.publication.version);
			timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		});
	}

}
