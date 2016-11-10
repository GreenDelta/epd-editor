package app.editors.epd;

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
import app.editors.RefTable;
import app.editors.RefText;
import app.rcp.Icon;
import app.util.Colors;
import app.util.TextBuilder;
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
		TextBuilder tb = new TextBuilder(editor, this, toolkit);
		createInfoSection(body, tb);
		new CategorySection(editor, dataSet).render(body, toolkit);
		RefTable.create(DataSetType.SOURCE, info.dataSetInfo.externalDocs)
				.withEditor(editor)
				.withTitle(M.ExternalDocumentationSources)
				.render(body, toolkit);
		createSafetyMarginsSection(body);
		createTimeSection(body, tb);
		createGeographySection(body, tb);
		createTechnologySection(body, tb);
		RefTable.create(DataSetType.SOURCE, info.technology.pictures)
				.withEditor(editor)
				.withTitle(M.FlowDiagramsOrPictures)
				.render(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite parent, TextBuilder tb) {
		Composite comp = UI.formSection(parent, toolkit,
				M.GeneralInformation);
		uuid(comp);
		tb.text(comp, M.Name,
				info.dataSetInfo.name.name);
		tb.text(comp, M.QuantitativeProperties,
				info.dataSetInfo.name.flowProperties);
		tb.text(comp, M.Synonyms,
				info.dataSetInfo.synonyms);
		tb.multiText(comp, M.Comment,
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
			editor.setDirty();
		});
	}

	private void modifyMargins(Text text) {
		String t = text.getText();
		SafetyMargins margins = dataSet.safetyMargins;
		if (Strings.nullOrEmpty(t)) {
			margins.margins = null;
			editor.setDirty();
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
		editor.setDirty();
	}

	private void createTechnologySection(Composite body, TextBuilder tb) {
		Technology tech = info.technology;
		Composite comp = UI.formSection(body, toolkit, M.Technology);
		tb.multiText(comp, M.TechnologyDescription,
				tech.description);
		tb.multiText(comp, M.TechnologicalApplicability,
				tech.applicability);
		UI.formLabel(comp, M.Pictogram);
		RefText refText = new RefText(comp, toolkit, DataSetType.SOURCE);
		UI.gridData(refText, true, false);
		refText.setRef(tech.pictogram);
		refText.onChange(ref -> {
			tech.pictogram = ref;
			editor.setDirty();
		});
	}

	private void createTimeSection(Composite body, TextBuilder tb) {
		Time time = info.time;
		Composite comp = UI.formSection(body, toolkit, M.Time);
		intText(comp, M.ReferenceYear, time.referenceYear, val -> {
			time.referenceYear = val;
		});
		intText(comp, M.ValidUntil, time.validUntil, val -> {
			time.validUntil = val;
		});
		tb.multiText(comp, M.TimeDescription, time.description);
	}

	private void createGeographySection(Composite body, TextBuilder tb) {
		Location location = info.geography.location;
		Composite comp = UI.formSection(body, toolkit, M.Geography);
		toolkit.createLabel(comp, M.Location);
		LocationCombo viewer = new LocationCombo();
		viewer.create(comp, location.code, code -> {
			location.code = code;
			editor.setDirty();
		});
		tb.multiText(comp, M.GeographyDescription, location.description);
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
			editor.setDirty();
		});
	}
}