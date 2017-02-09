package app.editors.contact;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInfo;
import org.openlca.ilcd.util.Contacts;

import app.App;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class ContactPage extends FormPage {

	private final Contact contact;
	private final ContactEditor editor;
	private FormToolkit tk;

	ContactPage(ContactEditor editor) {
		super(editor, "ContactPage", "#Contact Data Set");
		this.editor = editor;
		contact = editor.contact;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> "#Contact: " + App.s(contact.getName());
		ScrolledForm form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.formSection(body, tk, "#Contact information");
		DataSetInfo info = Contacts.dataSetInfo(contact);
		tb.text(comp, "#Short name", info.shortName);
		tb.text(comp, "#Name", info.name);
		tb.text(comp, "#Address", info.contactAddress);
		tb.text(comp, "#Telephone", info.telephone, t -> info.telephone = t);
		tb.text(comp, "#Telefax", info.telefax, t -> info.telefax = t);
		tb.text(comp, "#WWW-Address", info.wwwAddress,
				t -> info.wwwAddress = t);
		UI.formLabel(comp, tk, "#Logo");
		RefLink logo = new RefLink(comp, tk, DataSetType.SOURCE);
		logo.setRef(info.logo);
		logo.onChange(ref -> {
			info.logo = ref;
			editor.setDirty();
		});
	}

	private void categorySection(Composite body) {
		DataSetInfo info = Contacts.dataSetInfo(contact);
		CategorySection section = new CategorySection(editor,
				DataSetType.CONTACT, info.classifications);
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		DataEntry entry = Contacts.dataEntry(contact);
		Publication pub = Contacts.publication(contact);
		Composite comp = UI.formSection(body, tk,
				"#Administrative information");
		Text timeT = UI.formText(comp, tk, "#Last change");
		timeT.setText(Xml.toString(entry.timeStamp));
		Text uuidT = UI.formText(comp, tk, "#UUID");
		if (contact.getUUID() != null)
			uuidT.setText(contact.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(contact.getVersion());
		vf.onChange(v -> pub.version = v);
		editor.onSaved(() -> {
			vf.setVersion(pub.version);
			timeT.setText(Xml.toString(entry.timeStamp));
		});
	}

}
