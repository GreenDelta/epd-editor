package app.editors.epd;

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
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.refs.RefLink;
import app.editors.refs.RefTable;
import app.editors.refs.RefTableSection;
import app.rcp.Texts;
import app.store.RefDeps;
import app.util.Colors;
import app.util.Controls;
import app.util.IntText;
import app.util.LangText;
import app.util.LangText.TextBuilder;
import app.util.UI;

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
		qRefSection(body, tb);

		RefTableSection.create(DataSetType.SOURCE)
				.withSupplier(() -> Epds.withDataSetInfo(epd).withExternalDocs())
				.withEditor(editor)
				.withTitle(M.ExternalDocumentationSources)
				.withTooltip(Tooltips.EPD_ExternalDocumentationSources)
				.render(body, tk);

		createSafetyMarginsSection(body, tb);
		createTimeSection(body, tb);
		createGeographySection(body, tb);
		createTechnologySection(body, tb);

		ServiceLifeSection.reference(editor, body, tk);
		ServiceLifeSection.estimated(editor, body, tk);

		new ManufacturerSection(editor).render(body, tk, form);

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

	private void qRefSection(Composite parent, TextBuilder tb) {
		var comp = UI.formSection(parent, tk,
				M.DeclaredProduct, Tooltips.EPD_DeclaredProduct);
		UI.formLabel(comp, tk, M.ProductFlow, Tooltips.EPD_DeclaredProduct);
		var refText = new RefLink(comp, tk, DataSetType.FLOW);
		var exchange = withProductExchange();
		refText.setRef(exchange.getFlow());
		Text amountText = UI.formText(comp, tk,
				M.Amount, Tooltips.EPD_ProductAmount);

		amountText.setText(Double.toString(exchange.getMeanAmount()));
		amountText.addModifyListener(_ -> {
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

		var qRef = epd.withProcessInfo().withQuantitativeReference();
		createQRefTypeCombo(comp, qRef);

		tb.nextMulti(M.FunctionalUnit, Tooltips.EPD_DeclaredProduct)
				.val(qRef.getFunctionalUnit())
				.edit(qRef::withFunctionalUnit)
				.draw(comp);

		ProductIdTable.create(editor, comp, tk);
	}

	/// The types of the quantitative reference in the order in which they are
	/// shown in the combo box.
	private static final QuantitativeReferenceType[] Q_REF_TYPES = {
		QuantitativeReferenceType.REFERENCE_FLOWS,
		QuantitativeReferenceType.FUNCTIONAL_UNIT,
		QuantitativeReferenceType.PRODUCTION_PERIOD,
		QuantitativeReferenceType.OTHER_PARAMETER
	};

	private void createQRefTypeCombo(
			Composite comp, QuantitativeReference qRef) {
		var combo = UI.formCombo(comp, tk,
				M.QuantitativeReference, Tooltips.EPD_DeclaredProduct);
		var items = new String[Q_REF_TYPES.length];
		int selected = -1;
		for (int i = 0; i < Q_REF_TYPES.length; i++) {
			items[i] = label(Q_REF_TYPES[i]);
			if (Q_REF_TYPES[i] == qRef.getType()) {
				selected = i;
			}
		}
		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, _ -> {
			int i = combo.getSelectionIndex();
			if (i < 0 || i >= Q_REF_TYPES.length)
				return;
			qRef.withType(Q_REF_TYPES[i]);
			editor.setDirty();
		});
	}

	private static String label(QuantitativeReferenceType type) {
		return switch (type) {
			case REFERENCE_FLOWS -> M.ReferenceFlow;
			case FUNCTIONAL_UNIT -> M.FunctionalUnit;
			case PRODUCTION_PERIOD -> M.ProductionPeriod;
			case OTHER_PARAMETER -> M.OtherParameter;
		};
	}

	private Exchange withProductExchange() {
		var qRef = epd.withProcessInfo()
				.withQuantitativeReference();
		// the type of the quantitative reference is set by the user;
		// we only set the default here
		if (qRef.getType() == null) {
			qRef.withType(QuantitativeReferenceType.REFERENCE_FLOWS);
		}
		if (qRef.getReferenceFlows().isEmpty()) {
			qRef.withReferenceFlows().add(1);
		}
		int id = qRef.getReferenceFlows().getFirst();
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

		// description
		tb.nextMulti(M.TechnologyDescription, Tooltips.EPD_TechnologyDescription)
				.val(tech.getDescription())
				.edit(tech::withDescription)
				.draw(comp);

		// purpose
		tb.nextMulti(M.TechnologicalApplicability, Tooltips.EPD_TechnicalPrupose)
				.val(tech.getApplicability())
				.edit(tech::withApplicability)
				.draw(comp);

		// pictogram
		UI.formLabel(comp, tk, M.Pictogram, Tooltips.EPD_Pictogram);
		var pictoLink = new RefLink(comp, tk, DataSetType.SOURCE);
		pictoLink.setRef(tech.getPictogram());
		pictoLink.onChange(ref -> {
			tech.withPictogram(ref);
			editor.setDirty();
		});

		// other pictures
		UI.formLabel(
			comp, tk, M.FlowDiagramsOrPictures, Tooltips.EPD_FlowDiagramsOrPictures);
		RefTable.create(DataSetType.SOURCE)
			.withSupplier(() -> Epds.withTechnology(epd).withPictures())
			.withEditor(editor)
			.withTooltip(Tooltips.EPD_FlowDiagramsOrPictures)
			.render(comp, tk);
	}

	private void createTimeSection(Composite body, TextBuilder tb) {
		var time = epd.withProcessInfo().withTime();
		var comp = UI.formSection(body, tk, M.Time, Tooltips.EPD_Time);
		IntText.on(editor, comp, tk)
				.withLabel(M.ReferenceYear)
				.withTooltip(Tooltips.EPD_ReferenceYear)
				.withInitial(time.getReferenceYear())
				.onChange(time::withReferenceYear)
				.render();
		IntText.on(editor, comp, tk)
				.withLabel(M.ValidUntil)
				.withTooltip(Tooltips.EPD_ValidUntil)
				.withInitial(time.getValidUntil())
				.onChange(time::withValidUntil)
				.render();

		// publication date
		tk.createLabel(comp, M.PublicationDate)
				.setToolTipText(Tooltips.EPD_PublicationDate);
		var pubBox = new DateTime(comp, SWT.DATE | SWT.DROP_DOWN);
		var pubDate = Epds.getPublicationDate(epd);
		if (pubDate != null) {
			pubBox.setDate(
					pubDate.getYear(),
					pubDate.getMonth() - 1,
					pubDate.getDay());
		}
		pubBox.addSelectionListener(Controls.onSelect(_ -> {
			// the date-box receives selection events by default,
			// the check if the value really changed here
			var next = DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar();
			next.setYear(pubBox.getYear());
			next.setMonth(pubBox.getMonth() + 1);
			next.setDay(pubBox.getDay());
			var prev = Epds.getPublicationDate(epd);
			if (prev == null
					|| prev.getYear() != next.getYear()
					|| prev.getMonth() != next.getMonth()
					|| prev.getDay() != next.getDay()) {
				Epds.withPublicationDate(epd, next);
				editor.setDirty();
			}
		}));

		// expiration date of EPD
		tk.createLabel(comp, M.ExpirationDateOfEPD)
				.setToolTipText(Tooltips.EPD_ExpirationDateOfEPD);
		var expBox = new DateTime(comp, SWT.DATE | SWT.DROP_DOWN);
		var expDate = Epds.getExpirationDate(epd);
		if (expDate != null) {
			expBox.setDate(
					expDate.getYear(),
					expDate.getMonth() - 1,
					expDate.getDay());
		}
		expBox.addSelectionListener(Controls.onSelect(_ -> {
			var next = DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar();
			next.setYear(expBox.getYear());
			next.setMonth(expBox.getMonth() + 1);
			next.setDay(expBox.getDay());
			var prev = Epds.getExpirationDate(epd);
			if (prev == null
					|| prev.getYear() != next.getYear()
					|| prev.getMonth() != next.getMonth()
					|| prev.getDay() != next.getDay()) {
				Epds.withExpirationDate(epd, next);
				editor.setDirty();
			}
		}));

		// description
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

}
