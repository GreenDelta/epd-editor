package app.editors.contact;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.Contacts;

import java.util.function.Supplier;

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
		var comp = UI.infoSection(contact, body, tk);
		var info = Contacts.withDataSetInfo(contact);
		tb.text(comp, M.ShortName, Tooltips.Contact_ShortName, info.withShortName());
		tb.text(comp, M.Name, Tooltips.Contact_Name, info.withName());
		tb.text(comp, M.Address, Tooltips.Contact_Address, info.withContactAddress());
		tb.text(comp, M.Telephone, Tooltips.Contact_Telephone, info.getTelephone(),
			info::withTelephone);
		tb.text(comp, M.Telefax, Tooltips.Contact_Telefax, info.getTelefax(),
			info::withTelefax);
		tb.text(comp, M.Website, Tooltips.Contact_Website, info.getWebSite(),
			info::withWebSite);
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
		var info = Contacts.withDataSetInfo(contact);
		var section = new CategorySection(editor,
			DataSetType.CONTACT, info.withClassifications());
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		var comp = UI.formSection(body, tk,
			M.AdministrativeInformation,
			Tooltips.All_AdministrativeInformation);

		Text timeT = UI.formText(comp, tk,
			M.LastUpdate, Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(Contacts.getTimeStamp(contact)));

		Text uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		var uuid = Contacts.getUUID(contact);
		if (uuid != null) {
			uuidT.setText(uuid);
		}

		var vf = new VersionField(comp, tk);
		vf.setVersion(Contacts.getVersion(contact));
		vf.onChange(v -> {
			Contacts.withVersion(contact, v);
			editor.setDirty();
		});

		editor.onSaved(() -> {
			vf.setVersion(Contacts.getVersion(contact));
			timeT.setText(Xml.toString(Contacts.getTimeStamp(contact)));
		});
	}

}
