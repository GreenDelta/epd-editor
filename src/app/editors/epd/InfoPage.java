package app.editors.epd;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.datatype.DatatypeFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.RefTable;
import app.rcp.Texts;
import app.store.RefDeps;
import app.util.Colors;
import app.util.Controls;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.EpdDataSet;
import epd.util.Strings;

class InfoPage extends FormPage {

	private final EpdEditor editor;
	private final EpdDataSet epd;
	private final Process process;

	private FormToolkit tk;

	public InfoPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.DataSetInformation);
		this.editor = editor;
		epd = editor.dataSet;
		process = epd.process;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		tk = mForm.getToolkit();
		ProcessName pName = process.withProcessInfo()
			.withDataSetInfo()
			.withProcessName();
		Supplier<String> title = () -> M.EPD + ": "
			+ App.s(pName.withBaseName());
		ScrolledForm form = UI.formHeader(mForm, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		Composite body = UI.formBody(form, mForm.getToolkit());
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		qRefSection(body);
		RefTable.create(DataSetType.SOURCE,
				process.withProcessInfo().withDataSetInfo().withExternalDocs())
			.withEditor(editor)
			.withTitle(M.ExternalDocumentationSources)
			.withTooltip(Tooltips.EPD_ExternalDocumentationSources)
			.render(body, tk);
		createSafetyMarginsSection(body, tb);
		createTimeSection(body, tb);
		createGeographySection(body, tb);
		createTechnologySection(body, tb);
		RefTable.create(DataSetType.SOURCE,
				process.withProcessInfo().withTechnology().withPictures())
			.withEditor(editor)
			.withTitle(M.FlowDiagramsOrPictures)
			.withTooltip(Tooltips.EPD_FlowDiagramsOrPictures)
			.render(body, tk);
		form.reflow(true);
	}

	private void infoSection(Composite parent, TextBuilder tb) {
		Composite comp = UI.infoSection(process, parent, tk);
		ProcessName pName = process.withProcessInfo().withDataSetInfo().withProcessName();
		tb.text(comp, M.Name, Tooltips.EPD_Name, pName.withBaseName());
		tb.text(comp, M.QuantitativeProperties,
			Tooltips.EPD_FurtherProperties, pName.withFlowProperties());
		DataSetInfo info = process.withProcessInfo().withDataSetInfo();
		tb.text(comp, M.Synonyms, Tooltips.EPD_Synonyms, info.withSynonyms());
		tb.multiText(comp, M.Comment, Tooltips.EPD_Comment, info.withComment());
		UI.fileLink(process, comp, tk);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = process.withProcessInfo().withDataSetInfo();
		CategorySection section = new CategorySection(editor,
			DataSetType.PROCESS, info.withClassifications());
		section.render(body, tk);
	}

	private void qRefSection(Composite parent) {
		Composite comp = UI.formSection(parent, tk,
			M.DeclaredProduct, Tooltips.EPD_DeclaredProduct);
		UI.formLabel(comp, tk, M.Product, Tooltips.EPD_DeclaredProduct);
		RefLink refText = new RefLink(comp, tk, DataSetType.FLOW);
		Exchange exchange = epd.productExchange();
		refText.setRef(exchange.withFlow());
		Text amountText = UI.formText(comp, tk,
			M.Amount, Tooltips.EPD_ProductAmount);
		amountText.setText(Double.toString(exchange.getMeanAmount()));
		amountText.addModifyListener(e -> {
			try {
				double val = Double.parseDouble(amountText.getText());
				exchange.withMeanAmount(val);
				exchange.withResultingAmount(val);
				amountText.setBackground(Colors.white());
			} catch (Exception ex) {
				amountText.setBackground(Colors.errorColor());
			}
		});
		Text unitText = UI.formText(comp, tk,
			M.Unit, Tooltips.EPD_ProductUnit);
		unitText.setText(RefDeps.getRefUnit(process));
		unitText.setEditable(false);
		refText.onChange(ref -> {
			exchange.withFlow(ref);
			unitText.setText(RefDeps.getRefUnit(process));
			editor.setDirty();
		});
	}

	private void createSafetyMarginsSection(Composite parent, TextBuilder tb) {
		Composite comp = UI.formSection(parent, tk,
			M.SafetyMargins, Tooltips.EPD_UncertaintyMargins);
		var margins = Epds.withSafetyMargins(epd.process);
		Text marginsText = UI.formText(comp, tk,
			M.SafetyMargin, Tooltips.EPD_UncertaintyMargins);
		Texts.set(marginsText, margins.getValue());
		Texts.validateNumber(marginsText, d -> {
			if (d.isEmpty()) {
				margins.withValue(null);
			} else {
				margins.withValue(d.getAsDouble());
			}
			editor.setDirty();
		});
		tb.multiText(comp, M.Description,
			Tooltips.EPD_UncertaintyMarginsDescription,
			margins.withDescription());
	}

	private void createTechnologySection(Composite body, TextBuilder tb) {
		Technology tech = process.withProcessInfo().withTechnology();
		Composite comp = UI.formSection(body, tk,
			M.Technology, Tooltips.EPD_Technology);
		tb.multiText(comp, M.TechnologyDescription,
			Tooltips.EPD_TechnologyDescription, tech.withDescription());
		tb.multiText(comp, M.TechnologicalApplicability,
			Tooltips.EPD_TechnicalPrupose, tech.withApplicability());
		UI.formLabel(comp, tk, M.Pictogram, Tooltips.EPD_Pictogram);
		RefLink refText = new RefLink(comp, tk, DataSetType.SOURCE);
		refText.setRef(tech.withPictogram());
		refText.onChange(ref -> {
			tech.withPictogram(ref);
			editor.setDirty();
		});
	}

	private void createTimeSection(Composite body, TextBuilder tb) {
		var time = process.withProcessInfo().withTime();
		var comp = UI.formSection(body, tk, M.Time, Tooltips.EPD_Time);
		intText(comp, M.ReferenceYear, Tooltips.EPD_ReferenceYear,
			time.getReferenceYear(), time::withReferenceYear);
		intText(comp, M.ValidUntil, Tooltips.EPD_ValidUntil,
			time.getValidUntil(), time::withValidUntil);

		// publication date
		tk.createLabel(comp, M.PublicationDate)
			.setToolTipText(Tooltips.EPD_PublicationDate);
		var dateBox = new DateTime(comp, SWT.DATE | SWT.DROP_DOWN);
		var pubDate = Epds.getPublicationDate(epd.process);
		if (pubDate != null) {
			dateBox.setDate(
				pubDate.getYear(),
				pubDate.getMonth() - 1,
				pubDate.getDay());
		}
		dateBox.addSelectionListener(Controls.onSelect(_e -> {
			// the date-box receives selection events by default,
			// the check if the value really changed here
			var next = DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar();
			next.setYear(dateBox.getYear());
			next.setMonth(dateBox.getMonth() + 1);
			next.setDay(dateBox.getDay());
			var prev = Epds.getPublicationDate(epd.process);
			if (prev == null
				|| prev.getYear() != next.getYear()
				|| prev.getMonth() != next.getMonth()
				|| prev.getDay() != next.getDay()) {
				Epds.withPublicationDate(epd.process, next);
				editor.setDirty();
			}
		}));

		tb.multiText(comp, M.TimeDescription,
			Tooltips.EPD_TimeDescription, time.withDescription());
	}

	private void createGeographySection(Composite body, TextBuilder tb) {
		Location location = process.withProcessInfo().withGeography().withLocation();
		Composite comp = UI.formSection(body, tk, M.Geography,
			Tooltips.EPD_Geography);
		tk.createLabel(comp, M.Location)
			.setToolTipText(Tooltips.EPD_Location);
		LocationCombo viewer = new LocationCombo();
		viewer.create(comp, location.getCode(), code -> {
			location.withCode(code);
			editor.setDirty();
		});
		tb.multiText(comp, M.GeographyDescription,
			Tooltips.EPD_GeographyDescription, location.withDescription());
	}

	private void intText(Composite comp, String label, String tooltip,
		Integer initial, Consumer<Integer> fn) {
		Text text = UI.formText(comp, tk, label, tooltip);
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
