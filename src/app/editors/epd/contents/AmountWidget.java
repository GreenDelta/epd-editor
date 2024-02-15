package app.editors.epd.contents;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import app.rcp.Texts;
import app.util.UI;
import epd.model.content.ContentAmount;

class AmountWidget {

	private final Text valText;
	private final Text lowText;
	private final Text uppText;

	AmountWidget(Composite parent, FormToolkit tk) {
		Composite comp = tk.createComposite(parent);
		UI.gridLayout(comp, 6, 10, 0);
		valText = UI.formText(comp, tk, "Value:");
		lowText = UI.formText(comp, tk, "Lower value:");
		uppText = UI.formText(comp, tk, "Upper value:");
		Arrays.asList(valText, lowText, uppText).forEach(t -> {
			UI.gridData(t, false, false).widthHint = 80;
			Texts.validateNumber(t);
		});
	}

	void setAmount(ContentAmount a) {
		if (a == null)
			return;
		Texts.set(valText, a.value);
		Texts.set(lowText, a.lowerValue);
		Texts.set(uppText, a.upperValue);
	}

	ContentAmount getAmount() {
		ContentAmount a = new ContentAmount();
		a.value = Texts.getDouble(valText);
		a.lowerValue = Texts.getDouble(lowText);
		a.upperValue = Texts.getDouble(uppText);
		return a;
	}

}
