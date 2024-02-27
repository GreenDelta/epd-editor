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
import app.M;
import app.Tooltips;
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
		super(editor, "ContactPage", M.Contact);
		this.editor = editor;
		contact = editor.contact;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.Contact + ": "
				+ App.s(Contacts.getName(contact));
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
		Composite comp = UI.infoSection(contact, body, tk);
		DataSetInfo info = contact.withContactInfo().withDataSetInfo();
		tb.text(comp, M.ShortName, Tooltips.Contact_ShortName, info.getShortName());
		tb.text(comp, M.Name, Tooltips.Contact_Name, info.getName());
		tb.text(comp, M.Address, Tooltips.Contact_Address, info.getContactAddress());
		tb.text(comp, M.Telephone, Tooltips.Contact_Telephone, info.getTelephone(),
				t -> info.withTelephone(t));
		tb.text(comp, M.Telefax, Tooltips.Contact_Telefax, info.getTelefax(),
				t -> info.withTelefax(t));
		tb.text(comp, M.Website, Tooltips.Contact_Website, info.getWebSite(),
				t -> info.withWebSite(t));
		UI.formLabel(comp, tk, M.Logo, Tooltips.Contact_Logo);
		RefLink logo = new RefLink(comp, tk, DataSetType.SOURCE);
		logo.setRef(info.getLogo());
		logo.onChange(ref -> {
			info.withLogo(ref);
			editor.setDirty();
		});
		UI.fileLink(contact, comp, tk);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = contact.withContactInfo().withDataSetInfo();
		CategorySection section = new CategorySection(editor,
				DataSetType.CONTACT, info.getClassifications());
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		DataEntry entry = Contacts.getDataEntry(contact);
		Publication pub = Contacts.getPublication(contact);
		Composite comp = UI.formSection(body, tk,
				M.AdministrativeInformation,
				Tooltips.All_AdministrativeInformation);
		Text timeT = UI.formText(comp, tk,
				M.LastUpdate, Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(entry.getTimeStamp()));
		Text uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		if (Contacts.getUUID(contact) != null)
			uuidT.setText(Contacts.getUUID(contact));
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(contact.getVersion());
		vf.onChange(v -> {
			pub.withVersion(v);
			editor.setDirty();
		});
		editor.onSaved(() -> {
			vf.setVersion(pub.getVersion());
			timeT.setText(Xml.toString(entry.getTimeStamp()));
		});
	}

}
