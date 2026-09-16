package app.editors.epd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdManufacturerVariability;
import org.openlca.ilcd.processes.epd.EpdProductVariability;
import org.openlca.ilcd.processes.epd.EpdVariationRange;
import org.openlca.ilcd.util.Epds;

import app.M;
import app.rcp.Labels;
import app.util.Controls;
import app.util.DoubleText;
import app.util.LangText;
import app.util.UI;
import app.util.Viewers;

class VariabilitySection {

	private static final Object NONE = new Object();

	private final EpdEditor editor;
	private final Process epd;

	VariabilitySection(EpdEditor editor) {
		this.editor = editor;
		this.epd = editor.epd;
	}

	void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.Variability);
		UI.gridLayout(comp, 3);

		// two columns for manufacturer and product variability
		UI.filler(comp, tk);
		UI.formLabel(comp, tk, M.ManufacturerVariability);
		UI.formLabel(comp, tk, M.ProductVariability);
		var v = Epds.getVariability(epd);

		// manufacturer variability type
		UI.formLabel(comp, tk, M.Type);
		makeEnumCombo(
			comp,
			EpdManufacturerVariability.VariabilityType.class,
			EpdManufacturerVariability.VariabilityType.values(),
			Labels::get,
			v != null && v.getManufacturerVariability() != null
				? v.getManufacturerVariability().getType()
				: null,
			type -> Epds.withVariability(epd)
				.withManufacturerVariability()
				.withType(type));

		// product variability type
		makeEnumCombo(
			comp,
			EpdProductVariability.VariabilityType.class,
			EpdProductVariability.VariabilityType.values(),
			Labels::get,
			v != null && v.getProductVariability() != null
				? v.getProductVariability().getType()
				: null,
			type -> Epds.withVariability(epd)
				.withProductVariability()
				.withType(type));

		//  manufacturer variation (%)
		UI.formLabel(comp, tk, M.VariationPercent);
		var manuVar = (v != null && v.getManufacturerVariability() != null)
			? v.getManufacturerVariability().getVariation()
			: null;
		DoubleText.on(editor, comp, tk)
			.withInitial(manuVar)
			.onChange(val -> Epds.withVariability(epd)
				.withManufacturerVariability()
				.withVariation(val))
			.render();

		// product variation (%)
		var prodVar = (v != null && v.getProductVariability() != null)
			? v.getProductVariability().getVariation()
			: null;
		DoubleText.on(editor, comp, tk)
			.withInitial(prodVar)
			.onChange(val -> Epds.withVariability(epd)
				.withProductVariability()
				.withVariation(val))
			.render();

		// manufacturer range
		UI.formLabel(comp, tk, M.VariationRange);
		makeEnumCombo(
			comp,
			EpdVariationRange.class,
			EpdVariationRange.values(),
			Labels::get,
			v != null && v.getManufacturerVariability() != null
				? v.getManufacturerVariability().getRange()
				: null,
			range -> Epds.withVariability(epd)
				.withManufacturerVariability()
				.withRange(range));

		// product range
		makeEnumCombo(
			comp,
			EpdVariationRange.class,
			EpdVariationRange.values(),
			Labels::get,
			v != null && v.getProductVariability() != null
				? v.getProductVariability().getRange()
				: null,
			range ->	Epds.withVariability(epd)
				.withProductVariability()
				.withRange(range));

		UI.formLabel(comp, tk, M.VariabilityDescription);
		var textComp = tk.createComposite(comp);
		UI.stretchX(textComp).horizontalSpan = 2;
		UI.innerGrid(textComp, 2);

		var descriptions = v != null ? v.getDescriptions() : null;
		LangText.builder(editor, tk)
			.nextMulti("")
			.val(descriptions)
			.edit(() -> Epds.withVariability(epd).withDescriptions())
			.draw(textComp);
	}


	private <T extends Enum<T>> void makeEnumCombo(
		Composite comp,
		Class<T> type,
		Enum<T>[] items,
		Function<T, String> labelProvider,
		T current,
		Consumer<T> onChange
	) {

		var combo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.stretchX(combo.getControl());
		combo.setContentProvider(
			ArrayContentProvider.getInstance());

		combo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object o) {
				if (o == null || o == NONE)
					return "";
				return type.isAssignableFrom(o.getClass())
					? labelProvider.apply(type.cast(o))
					: "";
			}
		});

		combo.setInput(nullableOf(items));
		var initial = current != null
			? current
			: NONE;
		combo.setSelection(new StructuredSelection(initial));

		Controls.onSelect(combo, e -> {
			var obj = Viewers.getFirst(e.getSelection());
			if (obj == null
				|| obj == NONE
				|| !type.isAssignableFrom(obj.getClass())) {
				onChange.accept(null);
			} else {
				onChange.accept(type.cast(obj));
			}
			editor.setDirty();
		});
	}

	private static <T> Object[] nullableOf(T[] values) {
		var list = new ArrayList<>();
		list.add(NONE);
		Collections.addAll(list, values);
		return list.toArray();
	}
}
