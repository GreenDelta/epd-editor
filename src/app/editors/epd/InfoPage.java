package app.editors.epd;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Processes;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.RefTable;
import app.store.RefDeps;
import app.util.Colors;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.EpdDataSet;
import epd.model.SafetyMargins;
import epd.util.Strings;

class InfoPage extends FormPage {

	private final EpdEditor editor;
	private final EpdDataSet dataSet;
	private final Process process;

	private FormToolkit tk;

	public InfoPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.DataSetInformation);
		this.editor = editor;
		dataSet = editor.dataSet;
		process = dataSet.process;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		tk = mForm.getToolkit();
		ProcessName pName = Processes.processName(process);
		Supplier<String> title = () -> M.EPD + ": " + App.s(pName.name);
		ScrolledForm form = UI.formHeader(mForm, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		Composite body = UI.formBody(form, mForm.getToolkit());
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		qRefSection(body);
		RefTable.create(DataSetType.SOURCE,
				Processes.dataSetInfo(process).externalDocs)
				.withEditor(editor)
				.withTitle(M.ExternalDocumentationSources)
				.render(body, tk);
		createSafetyMarginsSection(body, tb);
		createTimeSection(body, tb);
		createGeographySection(body, tb);
		createTechnologySection(body, tb);
		RefTable.create(DataSetType.SOURCE,
				Processes.technology(process).pictures)
				.withEditor(editor)
				.withTitle(M.FlowDiagramsOrPictures)
				.render(body, tk);
		form.reflow(true);
	}

	private void infoSection(Composite parent, TextBuilder tb) {
		Composite comp = UI.infoSection(process, parent, tk);
		ProcessName pName = Processes.processName(process);
		tb.text(comp, M.Name, Tooltips.EPD_Name, pName.name);
		tb.text(comp, M.QuantitativeProperties,
				Tooltips.EPD_FurtherProperties, pName.flowProperties);
		DataSetInfo info = Processes.dataSetInfo(process);
		tb.text(comp, M.Synonyms, Tooltips.EPD_Synonyms, info.synonyms);
		tb.multiText(comp, M.Comment, Tooltips.EPD_Comment, info.comment);
		UI.fileLink(process, comp, tk);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = Processes.dataSetInfo(process);
		CategorySection section = new CategorySection(editor,
				DataSetType.PROCESS, info.classifications);
		section.render(body, tk);
	}

	private void qRefSection(Composite parent) {
		Composite comp = UI.formSection(parent, tk,
				M.DeclaredProduct);
		UI.formLabel(comp, tk, M.Product);
		RefLink refText = new RefLink(comp, tk, DataSetType.FLOW);
		Exchange exchange = dataSet.productExchange();
		refText.setRef(exchange.flow);
		Text amountText = UI.formText(comp, tk, M.Amount);
		amountText.setText(Double.toString(exchange.meanAmount));
		amountText.addModifyListener(e -> {
			try {
				double val = Double.parseDouble(amountText.getText());
				exchange.meanAmount = val;
				exchange.resultingAmount = val;
				amountText.setBackground(Colors.white());
			} catch (Exception ex) {
				amountText.setBackground(Colors.errorColor());
			}
		});
		Text unitText = UI.formText(comp, tk, M.Unit);
		unitText.setText(RefDeps.getRefUnit(process));
		unitText.setEditable(false);
		refText.onChange(ref -> {
			exchange.flow = ref;
			unitText.setText(RefDeps.getRefUnit(process));
			editor.setDirty();
		});
	}

	private void createSafetyMarginsSection(Composite parent, TextBuilder tb) {
		Composite composite = UI.formSection(parent, tk,
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
		tb.multiText(composite, M.Description, margins.description);
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
		Technology tech = Processes.technology(process);
		Composite comp = UI.formSection(body, tk, M.Technology);
		tb.multiText(comp, M.TechnologyDescription,
				tech.description);
		tb.multiText(comp, M.TechnologicalApplicability,
				tech.applicability);
		UI.formLabel(comp, M.Pictogram);
		RefLink refText = new RefLink(comp, tk, DataSetType.SOURCE);
		refText.setRef(tech.pictogram);
		refText.onChange(ref -> {
			tech.pictogram = ref;
			editor.setDirty();
		});
	}

	private void createTimeSection(Composite body, TextBuilder tb) {
		Time time = Processes.time(process);
		Composite comp = UI.formSection(body, tk, M.Time);
		intText(comp, M.ReferenceYear, time.referenceYear, val -> {
			time.referenceYear = val;
		});
		intText(comp, M.ValidUntil, time.validUntil, val -> {
			time.validUntil = val;
		});
		tb.multiText(comp, M.TimeDescription, time.description);
	}

	private void createGeographySection(Composite body, TextBuilder tb) {
		Location location = Processes.location(process);
		Composite comp = UI.formSection(body, tk, M.Geography);
		tk.createLabel(comp, M.Location);
		LocationCombo viewer = new LocationCombo();
		viewer.create(comp, location.code, code -> {
			location.code = code;
			editor.setDirty();
		});
		tb.multiText(comp, M.GeographyDescription, location.description);
	}

	private void intText(Composite comp, String label, Integer initial,
			Consumer<Integer> fn) {
		Text text = UI.formText(comp, tk, label);
		if (initial != null)
			text.setText(initial.toString());
		text.addModifyListener(e -> {
			String s = text.getText();
			if (Strings.nullOrEmpty(s)) {
				fn.accept(null);
				editor.setDirty();
				return;
			}
			try {
				int i = Integer.parseInt(s.trim());
				fn.accept(i);
				text.setBackground(Colors.white());
			} catch (Exception ex) {
				text.setBackground(Colors.errorColor());
			}
			editor.setDirty();
		});
	}
}