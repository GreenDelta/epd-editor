package app.editors.epd.contents;

import app.rcp.Texts;
import app.util.UI;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.processes.epd.EpdContentAmount;

import java.util.Arrays;

class AmountWidget {

	private final Text valText;
	private final Text minText;
	private final Text maxText;

	AmountWidget(Composite parent, FormToolkit tk) {
		Composite comp = tk.createComposite(parent);
		UI.gridLayout(comp, 6, 10, 0);
		valText = UI.formText(comp, tk, "Value:");
		minText = UI.formText(comp, tk, "Lower value:");
		maxText = UI.formText(comp, tk, "Upper value:");
		Arrays.asList(valText, minText, maxText).forEach(t -> {
			UI.gridData(t, false, false).widthHint = 80;
			Texts.validateNumber(t);
		});
	}

	void setAmount(EpdContentAmount a) {
		if (a == null)
			return;
		Texts.set(valText, a.getValue());
		Texts.set(minText, a.getMin());
		Texts.set(maxText, a.getMax());
	}

	EpdContentAmount getAmount() {
		return new EpdContentAmount()
			.withValue(Texts.getDouble(valText))
			.withMin(Texts.getDouble(minText))
			.withMax(Texts.getDouble(maxText));
	}

}
