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
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
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
import app.util.LangText;
import app.util.LangText.TextBuilder;
import app.util.UI;
import epd.util.Strings;

class InfoPage extends FormPage {

	private final EpdEditor editor;
	private final Process epd;

	private FormToolkit tk;

	public InfoPage(EpdEditor editor) {
		super(editor, "EpdInfoPage", M.DataSetInformation);
		this.editor = editor;
		epd = editor.epd;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		tk = mForm.getToolkit();
		var pName = Epds.withProcessName(epd);
		Supplier<String> title = () -> M.EPD + ": "
				+ App.s(pName.withBaseName());
		var form = UI.formHeader(mForm, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		Composite body = UI.formBody(form, mForm.getToolkit());
		var tb = LangText.builder(editor, tk);
		infoSection(body, tb);
		categorySection(body);
		qRefSection(body);

		RefTable.create(DataSetType.SOURCE,
						Epds.withDataSetInfo(epd).withExternalDocs())
				.withEditor(editor)
				.withTitle(M.ExternalDocumentationSources)
				.withTooltip(Tooltips.EPD_ExternalDocumentationSources)
				.render(body, tk);

		createSafetyMarginsSection(body, tb);
		createTimeSection(body, tb);
		createGeographySection(body, tb);
		createTechnologySection(body, tb);

		RefTable.create(DataSetType.SOURCE,
						Epds.withTechnology(epd).withPictures())
				.withEditor(editor)
				.withTitle(M.FlowDiagramsOrPictures)
				.withTooltip(Tooltips.EPD_FlowDiagramsOrPictures)
				.render(body, tk);
		form.reflow(true);
	}

	private void infoSection(Composite parent, TextBuilder b) {
		var comp = UI.infoSection(epd, parent, tk);
		var pName = Epds.withProcessName(epd);

		b.next(M.Name, Tooltips.EPD_Name)
				.val(pName.getBaseName())
				.edit(pName::withBaseName)
				.draw(comp);

		b.next(M.QuantitativeProperties, Tooltips.EPD_FurtherProperties)
				.val(pName.getFlowProperties())
				.edit(pName::withFlowProperties)
				.draw(comp);

		var info = Epds.withDataSetInfo(epd);

		b.next(M.Synonyms, Tooltips.EPD_Synonyms)
				.val(info.getSynonyms())
				.edit(info::withSynonyms)
				.draw(comp);

		b.nextMulti(M.Comment, Tooltips.EPD_Comment)
				.val(info.getComment())
				.edit(info::withComment)
				.draw(comp);

		UI.fileLink(epd, comp, tk);
	}

	private void categorySection(Composite body) {
		var info = Epds.withDataSetInfo(epd);
		var section = new CategorySection(editor,
				DataSetType.PROCESS, info.withClassifications());
		section.render(body, tk);
	}

	private void qRefSection(Composite parent) {
		var comp = UI.formSection(parent, tk,
				M.DeclaredProduct, Tooltips.EPD_DeclaredProduct);
		UI.formLabel(comp, tk, M.ProductFlow, Tooltips.EPD_DeclaredProduct);
		var refText = new RefLink(comp, tk, DataSetType.FLOW);
		var exchange = withProductExchange();
		refText.setRef(exchange.getFlow());
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
		unitText.setText(RefDeps.getRefUnit(epd));
		unitText.setEditable(false);
		refText.onChange(ref -> {
			exchange.withFlow(ref);
			unitText.setText(RefDeps.getRefUnit(epd));
			editor.setDirty();
		});
	}

	public Exchange withProductExchange() {
		var qRef = epd.withProcessInfo()
				.withQuantitativeReference()
				.withType(QuantitativeReferenceType.REFERENCE_FLOWS);
		if (qRef.getReferenceFlows().isEmpty()) {
			qRef.withReferenceFlows().add(1);
		}
		int id = qRef.getReferenceFlows().get(0);
		for (var exchange : epd.getExchanges()) {
			if (id == exchange.getId())
				return exchange;
		}
		var e = new Exchange()
				.withId(id)
				.withMeanAmount(1d)
				.withResultingAmount(1d);
		epd.withExchanges().add(e);
		return e;
	}

	private void createSafetyMarginsSection(Composite parent, TextBuilder tb) {
		var comp = UI.formSection(parent, tk,
				M.SafetyMargins, Tooltips.EPD_UncertaintyMargins);
		var margins = Epds.withSafetyMargins(epd);
		var marginsText = UI.formText(comp, tk,
				M.SafetyMargin, Tooltips.EPD_UncertaintyMargins);
		Texts.set(marginsText, margins.getValue());
		Texts.validateNumber(marginsText, d -> {
			var val = d.isPresent()
					? d.getAsDouble()
					: null;
			margins.withValue(val);
			editor.setDirty();
		});

		tb.nextMulti(M.Description, Tooltips.EPD_UncertaintyMarginsDescription)
				.val(margins.getDescription())
				.edit(margins::withDescription)
				.draw(comp);
	}

	private void createTechnologySection(Composite body, TextBuilder tb) {
		var tech = epd.withProcessInfo().withTechnology();
		var comp = UI.formSection(
				body, tk, M.Technology, Tooltips.EPD_Technology);

		tb.nextMulti(M.TechnologyDescription, Tooltips.EPD_TechnologyDescription)
				.val(tech.getDescription())
				.edit(tech::withDescription)
				.draw(comp);

		tb.nextMulti(M.TechnologicalApplicability, Tooltips.EPD_TechnicalPrupose)
				.val(tech.getApplicability())
				.edit(tech::withApplicability)
				.draw(comp);

		UI.formLabel(comp, tk, M.Pictogram, Tooltips.EPD_Pictogram);
		var refText = new RefLink(comp, tk, DataSetType.SOURCE);
		refText.setRef(tech.getPictogram());
		refText.onChange(ref -> {
			tech.withPictogram(ref);
			editor.setDirty();
		});
	}

	private void createTimeSection(Composite body, TextBuilder tb) {
		var time = epd.withProcessInfo().withTime();
		var comp = UI.formSection(body, tk, M.Time, Tooltips.EPD_Time);
		intText(comp, M.ReferenceYear, Tooltips.EPD_ReferenceYear,
				time.getReferenceYear(), time::withReferenceYear);
		intText(comp, M.ValidUntil, Tooltips.EPD_ValidUntil,
				time.getValidUntil(), time::withValidUntil);

		// publication date
		tk.createLabel(comp, M.PublicationDate)
				.setToolTipText(Tooltips.EPD_PublicationDate);
		var dateBox = new DateTime(comp, SWT.DATE | SWT.DROP_DOWN);
		var pubDate = Epds.getPublicationDate(epd);
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
			var prev = Epds.getPublicationDate(epd);
			if (prev == null
					|| prev.getYear() != next.getYear()
					|| prev.getMonth() != next.getMonth()
					|| prev.getDay() != next.getDay()) {
				Epds.withPublicationDate(epd, next);
				editor.setDirty();
			}
		}));

		tb.nextMulti(M.TimeDescription, Tooltips.EPD_TimeDescription)
				.val(time.getDescription())
				.edit(time::withDescription)
				.draw(comp);
	}

	private void createGeographySection(Composite body, TextBuilder tb) {
		var loc = Epds.withLocation(epd);
		var comp = UI.formSection(body, tk, M.Geography, Tooltips.EPD_Geography);
		tk.createLabel(comp, M.Location)
				.setToolTipText(Tooltips.EPD_Location);
		var combo = new LocationCombo();
		combo.create(comp, loc.getCode(), code -> {
			loc.withCode(code);
			editor.setDirty();
		});

		tb.nextMulti(M.GeographyDescription, Tooltips.EPD_GeographyDescription)
				.val(loc.getDescription())
				.edit(loc::withDescription)
				.draw(comp);
	}

	private void intText(
			Composite comp, String label, String tooltip,
			Integer initial, Consumer<Integer> fn
	) {
		var text = UI.formText(comp, tk, label, tooltip);
		if (initial != null) {
			text.setText(initial.toString());
		}
		text.addModifyListener(e -> {
			var s = text.getText();
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
