package app.editors.epd.contents;

import app.App;
import app.M;
import app.rcp.Texts;
import app.util.UI;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.processes.epd.EpdContentElement;
import org.openlca.ilcd.processes.epd.EpdInnerContentElement;

import java.util.Arrays;

class ContentDialog extends FormDialog {

	private final EpdContentDeclaration decl;
	private final EpdContentElement<?> elem;
	private EpdContentElement<?> parent;

	// UI components
	private Text nameText;
	private AmountWidget massPercWidget;
	private AmountWidget massWidget;
	private Text casText;
	private Text ecText;
	private Text guuidText;
	private Text renewableText;
	private Text recycledText;
	private Text recycableText;
	private Text commentText;

	static int open(EpdContentDeclaration decl, EpdContentElement<?> elem) {
		if (elem == null)
			return CANCEL;
		var d = new ContentDialog(decl, elem);
		int code = d.open();
		if (code != OK)
			return code;
		Content.remove(decl, elem);
		boolean added = false;
		if (d.parent != null) {
			added = Content.addChild(d.parent, elem);
		}
		if (!added) {
			decl.withElements().add(elem);
		}
		return OK;
	}

	ContentDialog(EpdContentDeclaration decl, EpdContentElement<?> elem) {
		super(UI.shell());
		this.decl = decl;
		this.elem = elem;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {

		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(mform.getForm(), tk);

		Composite comp = UI.formComposite(body, tk);
		UI.gridData(comp, true, false);
		UI.gridLayout(comp, 3);

		// the parent combo
		if (Content.canHaveParent(elem, decl)) {
			Combo combo = UI.formCombo(comp, tk, "Parent element");
			UI.gridData(combo, true, false);
			UI.filler(comp, tk);
			new ParentCombo(decl, elem).bind(combo).onChange(
				p -> this.parent = p);
			this.parent = Content.getParent(elem, decl);
		}

		// name
		nameText = UI.formText(comp, tk, M.Name);
		Texts.set(nameText, App.s(elem.getName()));
		UI.gridData(nameText, true, false).widthHint = 350;
		UI.filler(comp, tk);

		// weight percentage
		UI.formLabel(comp, tk, "Weight percentage");
		massPercWidget = new AmountWidget(comp, tk);
		massPercWidget.setAmount(elem.getWeightPerc());
		UI.formLabel(comp, tk, "%");

		// mass
		UI.formLabel(comp, tk, "Absolute mass");
		massWidget = new AmountWidget(comp, tk);
		massWidget.setAmount(elem.getMass());
		UI.formLabel(comp, tk, "kg");

		if (elem instanceof EpdInnerContentElement<?> inner) {
			// CAS
			casText = UI.formText(comp, tk, "CAS number");
			Texts.set(casText, inner.getCasNumber());
			UI.filler(comp, tk);

			// EC number
			ecText = UI.formText(comp, tk, "EC number");
			Texts.set(ecText, inner.getEcNumber());
			UI.filler(comp, tk);

			// GUUID
			guuidText = UI.formText(comp, tk,
				"Data dictionary GUUID");
			Texts.set(guuidText, inner.getGuid());
			UI.filler(comp, tk);

			// renewable resource
			renewableText = UI.formText(comp, tk,
				"Percentage of renewable resources");
			Texts.set(renewableText, inner.getRenewable());
			UI.formLabel(comp, tk, "%");

			// recycled content
			recycledText = UI.formText(comp, tk,
				"Percentage of recycled content");
			Texts.set(recycledText, inner.getRecycled());
			UI.formLabel(comp, tk, "%");

			// recyclable content
			recycableText = UI.formText(comp, tk,
				"Percentage of recyclable content");
			Texts.set(recycableText, inner.getRecyclable());
			UI.formLabel(comp, tk, "%");

			// add validation
			Arrays.asList(renewableText, recycledText, recycableText)
				.forEach(Texts::validateNumber);
		}

		commentText = UI.formMultiText(comp, tk, "Comment");
		Texts.set(commentText, App.s(elem.withComment()));
		UI.filler(comp, tk);
	}

	@Override
	protected void okPressed() {
		LangString.set(elem.withName(), nameText.getText(), App.lang());
		elem.withWeightPerc(massPercWidget.getAmount())
			.withMass(massWidget.getAmount());
		LangString.set(elem.withComment(), commentText.getText(), App.lang());
		if (elem instanceof EpdInnerContentElement<?> inner) {
			inner.withCasNumber(Texts.getString(casText))
				.withEcNumber(Texts.getString(ecText))
				.withGuid(Texts.getString(guuidText))
				.withRenewable(Texts.getDouble(renewableText))
				.withRecycled(Texts.getDouble(recycledText))
				.withRecyclable(Texts.getDouble(recycableText));
		}
		super.okPressed();
	}

}
