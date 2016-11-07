package app.editors.epd;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.Technology;

import app.App;
import app.M;
import app.editors.RefText;
import app.editors.TranslationView;
import app.rcp.Icon;
import app.util.Colors;
import app.util.UI;
import epd.model.EpdDataSet;
import epd.model.SafetyMargins;
import epd.util.Strings;

class InfoPage extends FormPage {

	private final EpdEditor editor;
	private final String lang;
	private final EpdDataSet dataSet;
	private final ProcessInfo info;

	private FormToolkit toolkit;

	public InfoPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.DataSetInformation);
		this.editor = editor;
		this.lang = App.lang;
		dataSet = editor.getDataSet();
		info = dataSet.processInfo;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		toolkit = mForm.getToolkit();
		String name = LangString.getFirst(info.dataSetInfo.name.name, lang);
		ScrolledForm form = UI.formHeader(mForm, M.EPD + ": " + name);
		Composite body = UI.formBody(form, mForm.getToolkit());
		createInfoSection(body);
		new CategorySection(editor, dataSet).render(body, toolkit);
		SourceTable.create(info.dataSetInfo.externalDocs, lang)
				.withEditor(editor)
				.withTitle(M.ExternalDocumentationSources)
				.render(body, toolkit);
		createSafetyMarginsSection(body);
		createTimeSection(body);
		createGeographySection(body);
		createTechnologySection(body);
		SourceTable.create(info.technology.pictures, lang)
				.withEditor(editor)
				.withTitle(M.FlowDiagramsOrPictures)
				.render(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite parent) {
		Composite comp = UI.formSection(parent, toolkit,
				M.GeneralInformation);
		uuid(comp);
		text(comp, M.Name,
				info.dataSetInfo.name.name);
		text(comp, M.QuantitativeProperties,
				info.dataSetInfo.name.flowProperties);
		text(comp, M.Synonyms,
				info.dataSetInfo.synonyms);
		multiText(comp, M.Comment,
				info.dataSetInfo.comment);
		createFileLink(comp);
	}

	private void uuid(Composite comp) {
		Text text = UI.formText(comp, toolkit, M.UUID);
		text.setEditable(false);
		if (info.dataSetInfo.uuid != null)
			text.setText(info.dataSetInfo.uuid);
	}

	private void createFileLink(Composite comp) {
		UI.formLabel(comp, toolkit, M.File);
		ImageHyperlink link = toolkit.createImageHyperlink(comp, SWT.NONE);
		link.setForeground(Colors.linkBlue());
		link.setImage(Icon.DOCUMENT.img());
		String uuid = info.dataSetInfo.uuid;
		link.setText("../processes/" + uuid + ".xml");
		// TODO:
		// Controls.onClick(link, e -> Browser.openFile(dataSet,
		// Plugin.getEpdStore()));
	}

	private void createSafetyMarginsSection(Composite parent) {
		Composite composite = UI.formSection(parent, toolkit,
				M.SafetyMargins);
		SafetyMargins margins = dataSet.safetyMargins;
		if (margins == null) {
			margins = new SafetyMargins();
			dataSet.safetyMargins = margins;
		}
		Text marginsText = UI.formText(composite, M.SafetyMargin);
		if (margins.margins != null) {
			marginsText.setText(margins.margins.toString());
		}
		marginsText.addModifyListener(e -> modifyMargins(marginsText));
		Text text = UI.formMultiText(composite, toolkit, M.Description);
		if (margins.description != null)
			text.setText(margins.description);
		text.addModifyListener(e -> {
			dataSet.safetyMargins.description = text.getText();
			editor.setDirty(true);
		});
	}

	private void modifyMargins(Text text) {
		String t = text.getText();
		SafetyMargins margins = dataSet.safetyMargins;
		if (Strings.nullOrEmpty(t)) {
			margins.margins = null;
			editor.setDirty(true);
			return;
		}
		try {
			margins.margins = Double.parseDouble(t);
		} catch (Exception e) {
			if (margins.margins != null)
				text.setText(margins.margins.toString());
			else
				text.setText("");
		}
		editor.setDirty(true);
	}

	private void createTechnologySection(Composite body) {
		Technology tech = info.technology;
		Composite comp = UI.formSection(body, toolkit, M.Technology);
		multiText(comp, M.TechnologyDescription, tech.description);
		multiText(comp, M.TechnologicalApplicability,
				tech.applicability);
		UI.formLabel(comp, M.Pictogram);
		RefText refText = new RefText(comp, toolkit, DataSetType.SOURCE);
		UI.gridData(refText, true, false);
		refText.setRef(tech.pictogram);
		refText.onChange(ref -> {
			tech.pictogram = ref;
			editor.setDirty(true);
		});
	}

	private void createTimeSection(Composite body) {
		Time time = info.time;
		Composite comp = UI.formSection(body, toolkit, M.Time);
		intText(comp, M.ReferenceYear, time.referenceYear, val -> {
			time.referenceYear = val;
		});
		intText(comp, M.ValidUntil, time.validUntil, val -> {
			time.validUntil = val;
		});
		multiText(comp, M.TimeDescription, time.description);
	}

	private void createGeographySection(Composite body) {
		Location location = info.geography.location;
		Composite comp = UI.formSection(body, toolkit, M.Geography);
		toolkit.createLabel(comp, M.Location);
		LocationCombo viewer = new LocationCombo();
		viewer.create(comp, location.code, code -> {
			location.code = code;
			editor.setDirty(true);
		});
		multiText(comp, M.GeographyDescription, location.description);
	}

	private void text(Composite comp, String label, List<LangString> list) {
		Text text = UI.formText(comp, toolkit, label);
		text(text, label, list);
	}

	private void multiText(Composite comp, String label,
			List<LangString> list) {
		Text text = UI.formMultiText(comp, toolkit, label);
		text(text, label, list);
	}

	private void text(Text text, String label, List<LangString> list) {
		String val = LangString.getVal(list, lang);
		if (val != null)
			text.setText(val);
		text.addModifyListener(e -> {
			LangString.set(list, text.getText(), lang);
			editor.setDirty(true);
		});
		TranslationView.register(this, label, text, list);
	}

	private void intText(Composite comp, String label, Integer initial,
			Consumer<Integer> fn) {
		Text text = UI.formText(comp, toolkit, label);
		if (initial != null)
			text.setText(initial.toString());
		text.addModifyListener(e -> {
			try {
				int i = Integer.parseInt(text.getText().trim());
				fn.accept(i);
				text.setBackground(Colors.white());
			} catch (Exception ex) {
				text.setBackground(Colors.errorColor());
			}
			editor.setDirty(true);
		});
	}
}