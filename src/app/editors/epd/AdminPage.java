package app.editors.epd;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.LicenseType;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

import app.M;
import app.Tooltips;
import app.editors.RefLink;
import app.editors.RefTable;
import app.editors.VersionField;
import app.util.Controls;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;
import epd.util.Strings;

class AdminPage extends FormPage {

	private final EpdEditor editor;
	private final Process process;

	public AdminPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.AdministrativeInformation);
		this.editor = editor;
		process = editor.dataSet.process;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		var form = UI.formHeader(mform, M.AdministrativeInformation);
		var body = UI.formBody(form, mform.getToolkit());

		// project
		projectSection(body, tk);

		// commissioner
		var commissioners = Processes.getCommissionerAndGoal(process)
				.withCommissioners();
		RefTable.create(DataSetType.CONTACT, commissioners)
				.withEditor(editor)
				.withTitle(M.Commissioner)
				.withTooltip(Tooltips.EPD_Commissioner)
				.render(body, tk);

		// data entry
		dataEntrySection(body, tk);

		// data set generators
		var generators = Processes.getDataGenerator(process).withContacts();
		RefTable.create(DataSetType.CONTACT, generators)
				.withEditor(editor)
				.withTitle(M.DataSetGeneratorModeller)
				.withTooltip(Tooltips.EPD_DataSetGeneratorModeller)
				.render(body, tk);

		// data formats
		var formats = Processes.getDataEntry(process).withFormats();
		RefTable.create(DataSetType.SOURCE, formats)
				.withEditor(editor)
				.withTitle(M.DataFormats)
				.withTooltip(Tooltips.EPD_DataFormats)
				.render(body, tk);

		// publication and ownership
		publicationSection(body, tk);

		// publishers
		RefTable.create(DataSetType.CONTACT, editor.dataSet.publishers)
				.withEditor(editor)
				.withTitle(M.Publisher)
				.withTooltip(Tooltips.EPD_Publisher)
				.render(body, tk);

		// preceding data version
		var precedingVersions = Processes
				.getPublication(process).withPrecedingVersions();
		RefTable.create(DataSetType.PROCESS, precedingVersions)
				.withEditor(editor)
				.withTitle(M.PrecedingDataSetVersion)
				.withTooltip(Tooltips.EPD_PrecedingDataSetVersion)
				.render(body, tk);

		form.reflow(true);
	}

	private void projectSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.Project);
		var goal = Processes.getCommissionerAndGoal(process);

		// project
		new TextBuilder(editor, this, tk).multiText(
				comp,
				M.Project,
				Tooltips.EPD_Project,
				goal.withProject());

		// intended applications
		new TextBuilder(editor, this, tk).multiText(
				comp,
				M.IntendedApplications,
				Tooltips.EPD_IntendedApplications,
				goal.withIntendedApplications());
	}

	private void dataEntrySection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk,
				M.DataEntry, Tooltips.EPD_DataEntry);
		var entry = Processes.getDataEntry(process);

		// last update
		var lastUpdate = UI.formText(comp, tk,
				M.LastUpdate, Tooltips.All_LastUpdate);
		lastUpdate.setEditable(false);
		editor.onSaved(() -> {
			XMLGregorianCalendar t = entry.getTimeStamp();
			lastUpdate.setText(Xml.toString(t));
		});
		if (entry.getTimeStamp() != null) {
			lastUpdate.setText(Xml.toString(entry.getTimeStamp()));
		}

		// documentor
		UI.formLabel(comp, tk, M.Documentor, Tooltips.EPD_Documentor);
		var documentor = new RefLink(comp, tk, DataSetType.CONTACT);
		documentor.setRef(entry.withDocumentor());
		documentor.onChange(ref -> {
			entry.withDocumentor(ref);
			editor.setDirty();
		});
	}

	private void publicationSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk,
				M.PublicationAndOwnership,
				Tooltips.EPD_PublicationAndOwnership);
		var pub = Processes.getPublication(process);

		// version
		var version = new VersionField(comp, tk);
		version.setVersion(pub.getVersion());
		editor.onSaved(() -> version.setVersion(pub.getVersion()));
		version.onChange(v -> {
			pub.withVersion(v);
			editor.setDirty();
		});

		// registration authority
		UI.formLabel(comp, tk, M.RegistrationAuthority,
				Tooltips.EPD_RegistrationAuthority);
		var regAuthority = new RefLink(comp, tk, DataSetType.CONTACT);
		regAuthority.setRef(pub.withRegistrationAuthority());
		regAuthority.onChange(ref -> {
			pub.withRegistrationAuthority(ref);
			editor.setDirty();
		});

		// registration number
		var regNumber = UI.formText(comp, tk, M.RegistrationNumber,
				Tooltips.EPD_RegistrationNumber);
		if (pub.getRegistrationNumber() != null) {
			regNumber.setText(pub.getRegistrationNumber());
		}
		regNumber.addModifyListener(e -> {
			String number = regNumber.getText();
			if (Strings.nullOrEmpty(number)) {
				pub.withRegistrationNumber(null);
			} else {
				pub.withRegistrationNumber(number);
			}
		});

		// owner
		UI.formLabel(comp, tk, M.Owner, Tooltips.EPD_Owner);
		var owner = new RefLink(comp, tk, DataSetType.CONTACT);
		owner.setRef(pub.withOwner());
		owner.onChange(ref -> {
			pub.withOwner(ref);
			editor.setDirty();
		});

		// copyright
		var copyright = UI.formCheckBox(comp, tk,
				M.Copyright, Tooltips.EPD_Copyright);
		copyright
				.setSelection(pub.getCopyright() != null && pub.getCopyright());
		Controls.onSelect(copyright, e -> {
			pub.withCopyright(copyright.getSelection());
			editor.setDirty();
		});

		// license
		licenseCombo(comp, tk);

		// access restrictions
		new TextBuilder(editor, this, tk).multiText(
				comp,
				M.AccessRestrictions,
				Tooltips.EPD_AccessRestrictions,
				pub.withAccessRestrictions());
	}

	private void licenseCombo(Composite comp, FormToolkit tk) {
		// TODO: labels, translations and tool-tips

		// map the combo items
		var pub = Processes.getPublication(process);
		var types = LicenseType.values();
		var items = new String[types.length + 1];
		items[0] = "";
		int selected = 0;
		for (int i = 0; i < types.length; i++) {
			items[i + 1] = types[i].value();
			if (pub.getLicense() == types[i]) {
				selected = i + 1;
			}
		}

		// create the combo
		var combo = UI.formCombo(comp, tk, M.LicenseType,
				Tooltips.EPD_LicenseType);
		combo.setItems(items);
		combo.select(selected);
		Controls.onSelect(combo, e -> {
			int i = combo.getSelectionIndex();
			if (i == 0) {
				pub.withLicense(null);
			} else {
				pub.withLicense(types[i - 1]);
			}
			editor.setDirty();
		});
	}
}
