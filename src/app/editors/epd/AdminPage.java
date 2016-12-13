package app.editors.epd;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.util.Processes;

import app.M;
import app.editors.RefLink;
import app.editors.VersionField;
import app.util.Controls;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class AdminPage extends FormPage {

	private FormToolkit toolkit;

	private EpdEditor editor;
	private Process process;

	public AdminPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.AdministrativeInformation);
		this.editor = editor;
		process = editor.dataSet.process;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		toolkit = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform,
				M.AdministrativeInformation);
		Composite body = UI.formBody(form, mform.getToolkit());
		createDataEntrySection(body);
		createPublicationSection(body);
		form.reflow(true);
	}

	private void createDataEntrySection(Composite parent) {
		Composite comp = UI.formSection(parent, toolkit,
				M.DataEntry);
		createLastUpdateText(comp);
		UI.formLabel(comp, M.Documentor);
		RefLink t = new RefLink(comp, toolkit, DataSetType.CONTACT);
		DataEntry entry = Processes.dataEntry(process);
		t.setRef(entry.documentor);
		t.onChange(ref -> {
			entry.documentor = ref;
			editor.setDirty();
		});
	}

	private void createLastUpdateText(Composite comp) {
		Text text = UI.formText(comp, toolkit, M.LastUpdate);
		text.setEditable(false);
		DataEntry entry = Processes.dataEntry(process);
		editor.onSaved(() -> {
			XMLGregorianCalendar t = entry.timeStamp;
			text.setText(Xml.toString(t));
		});
		if (entry.timeStamp == null)
			return;
		String s = Xml.toString(entry.timeStamp);
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
		RefLink t = new RefLink(comp, toolkit, DataSetType.CONTACT);
		Publication pub = Processes.publication(process);
		t.setRef(pub.owner);
		t.onChange(ref -> {
			pub.owner = ref;
			editor.setDirty();
		});

	}

	private void copyright(Composite composite) {
		Button check = UI.formCheckBox(composite, toolkit, M.Copyright);
		Publication pub = Processes.publication(process);
		if (pub.copyright != null)
			check.setSelection(pub.copyright);
		Controls.onSelect(check, e -> {
			pub.copyright = check.getSelection();
			editor.setDirty();
		});
	}

	private void version(Composite comp) {
		VersionField v = new VersionField(comp, toolkit);
		Publication pub = Processes.publication(process);
		v.setVersion(pub.version);
		editor.onSaved(() -> v.setVersion(pub.version));
		v.onChange(version -> {
			pub.version = version;
			editor.setDirty();
		});
	}

	private void accessRestrictions(Composite comp) {
		Publication pub = Processes.publication(process);
		new TextBuilder(editor, this, toolkit)
				.text(comp, M.AccessRestrictions,
						pub.accessRestrictions);
	}
}
