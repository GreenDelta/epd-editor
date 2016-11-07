package app.editors.epd;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.Publication;

import app.App;
import app.M;
import app.editors.RefText;
import app.editors.TranslationView;
import app.editors.VersionField;
import app.util.Controls;
import app.util.UI;
import epd.model.Xml;

class AdminPage extends FormPage {

	private FormToolkit toolkit;

	private EpdEditor editor;
	private AdminInfo adminInfo;

	public AdminPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.AdministrativeInformation);
		this.editor = editor;
		adminInfo = editor.getDataSet().adminInfo;
		if (adminInfo == null) {
			adminInfo = new AdminInfo();
			editor.getDataSet().adminInfo = adminInfo;
		}
		if (adminInfo.dataEntry == null)
			adminInfo.dataEntry = new DataEntry();
		if (adminInfo.publication == null)
			adminInfo.publication = new Publication();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		toolkit = managedForm.getToolkit();
		ScrolledForm form = UI.formHeader(managedForm,
				M.AdministrativeInformation);
		Composite body = UI.formBody(form, managedForm.getToolkit());
		createDataEntrySection(body);
		createPublicationSection(body);
		form.reflow(true);
	}

	private void createDataEntrySection(Composite parent) {
		Composite comp = UI.formSection(parent, toolkit,
				M.DataEntry);
		createLastUpdateText(comp);
		UI.formLabel(comp, M.Documentor);
		RefText t = new RefText(comp, toolkit, DataSetType.CONTACT);
		UI.gridData(t, true, false);
		t.setRef(adminInfo.dataEntry.documentor);
		t.onChange(ref -> {
			adminInfo.dataEntry.documentor = ref;
			editor.setDirty(true);
		});
	}

	private void createLastUpdateText(Composite comp) {
		Text text = UI.formText(comp, toolkit, M.LastUpdate);
		text.setEditable(false);
		editor.onSaved(() -> {
			XMLGregorianCalendar t = adminInfo.dataEntry.timeStamp;
			text.setText(Xml.toString(t));
		});
		if (adminInfo.dataEntry.timeStamp == null)
			return;
		String s = Xml.toString(adminInfo.dataEntry.timeStamp);
		text.setText(s);
	}

	private void createPublicationSection(Composite parent) {
		Composite comp = UI.formSection(parent, toolkit,
				M.PublicationAndOwnership);
		version(comp);
		owner(comp);
		copyright(comp);
		accessRestrictions(comp);
	}

	private void owner(Composite comp) {
		UI.formLabel(comp, M.Owner);
		RefText t = new RefText(comp, toolkit, DataSetType.CONTACT);
		UI.gridData(t, true, false);
		t.setRef(adminInfo.publication.owner);
		t.onChange(ref -> {
			adminInfo.publication.owner = ref;
			editor.setDirty(true);
		});

	}

	private void copyright(Composite composite) {
		Button check = UI.formCheckBox(composite, toolkit, M.Copyright);
		Boolean b = adminInfo.publication.copyright;
		if (b != null)
			check.setSelection(b);
		Controls.onSelect(check, e -> {
			adminInfo.publication.copyright = check.getSelection();
			editor.setDirty(true);
		});
	}

	private void version(Composite comp) {
		VersionField v = new VersionField(comp, toolkit);
		v.setVersion(adminInfo.publication.version);
		editor.onSaved(() -> v.setVersion(adminInfo.publication.version));
		v.onChange(version -> {
			adminInfo.publication.version = version;
			editor.setDirty(true);
		});
	}

	private void accessRestrictions(Composite comp) {
		List<LangString> strings = adminInfo.publication.accessRestrictions;
		Text t = UI.formMultiText(comp, toolkit,
				M.AccessRestrictions);
		String s = LangString.getVal(strings, App.lang);
		if (s != null)
			t.setText(s);
		TranslationView.register(this, M.AccessRestrictions, t, strings);
		t.addModifyListener(e -> {
			LangString.set(strings, t.getText(), App.lang);
			editor.setDirty(true);
		});
	}
}
