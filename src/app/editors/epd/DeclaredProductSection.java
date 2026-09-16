package app.editors.epd;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.QuantitativeReference;

import app.M;
import app.Tooltips;
import app.editors.refs.RefLink;
import app.store.RefDeps;
import app.util.Colors;
import app.util.Controls;
import app.util.LangText;
import app.util.UI;

/// The section with the declared product of an EPD and its quantitative
/// reference. The declared product is the exchange that is referenced by the
/// quantitative reference of the EPD; when it does not exist yet, it is created
/// when this section is rendered.
class DeclaredProductSection {

	/// The types of the quantitative reference in the order in which they are
	/// shown in the combo box.
	private static final QuantitativeReferenceType[] Q_REF_TYPES = {
		QuantitativeReferenceType.REFERENCE_FLOWS,
		QuantitativeReferenceType.FUNCTIONAL_UNIT,
		QuantitativeReferenceType.PRODUCTION_PERIOD,
		QuantitativeReferenceType.OTHER_PARAMETER
	};

	private final EpdEditor editor;
	private final Process epd;

	private DeclaredProductSection(EpdEditor editor) {
		this.editor = editor;
		this.epd = editor.epd;
	}

	static void create(EpdEditor editor, Composite body, FormToolkit tk) {
		new DeclaredProductSection(editor).render(body, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk,
				M.DeclaredProduct, Tooltips.EPD_DeclaredProduct);
		UI.formLabel(comp, tk, M.ProductFlow, Tooltips.EPD_DeclaredProduct);
		var refText = new RefLink(comp, tk, DataSetType.FLOW);
		var exchange = ensureProductExchange();
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
		createQRefTypeCombo(comp, tk, qRef);

		LangText.builder(editor, tk)
				.nextMulti(M.FunctionalUnit, Tooltips.EPD_DeclaredProduct)
				.val(qRef.getFunctionalUnit())
				.edit(qRef::withFunctionalUnit)
				.draw(comp);

		ProductIdTable.create(editor, comp, tk);
	}

	private void createQRefTypeCombo(
			Composite comp, FormToolkit tk, QuantitativeReference qRef) {
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

	/// Returns the exchange of the declared product, i.e. the exchange that is
	/// the reference flow of the quantitative reference of the EPD. When this
	/// exchange does not exist yet, it is created with an amount of one.
	private Exchange ensureProductExchange() {
		var qRef = epd.withProcessInfo().withQuantitativeReference();
		// the type of the quantitative reference is set by the user;
		// we only set the default here
		if (qRef.getType() == null) {
			qRef.withType(QuantitativeReferenceType.REFERENCE_FLOWS);
		}
		if (qRef.getReferenceFlows().isEmpty()) {
			qRef.withReferenceFlows().add(1);
		}
		var exchange = RefDeps.getRefExchange(epd);
		if (exchange != null)
			return exchange;
		var e = new Exchange()
				.withId(qRef.getReferenceFlows().getFirst())
				.withMeanAmount(1d)
				.withResultingAmount(1d);
		epd.withExchanges().add(e);
		return e;
	}

}
