package app.editors.epd.contents;

import java.util.Arrays;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.App;
import app.M;
import app.rcp.Texts;
import app.util.UI;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.model.content.Substance;

class ContentDialog extends FormDialog {

	private final ContentDeclaration decl;
	private final ContentElement elem;
	private ContentElement parent;

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

	static int open(ContentDeclaration decl, ContentElement elem) {
		if (elem == null)
			return CANCEL;
		ContentDialog d = new ContentDialog(decl, elem);
		int code = d.open();
		if (code != OK)
			return code;
		Content.remove(decl, elem);
		boolean added = false;
		if (d.parent != null) {
			added = Content.addChild(d.parent, elem);
		}
		if (!added) {
			decl.content.add(elem);
		}
		return OK;
	}

	ContentDialog(ContentDeclaration decl, ContentElement elem) {
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
		Texts.set(nameText, App.s(elem.name));
		UI.gridData(nameText, true, false).widthHint = 350;
		UI.filler(comp, tk);

		// weight percentage
		UI.formLabel(comp, tk, "Weight percentage");
		massPercWidget = new AmountWidget(comp, tk);
		massPercWidget.setAmount(elem.massPerc);
		UI.formLabel(comp, tk, "%");

		// mass
		UI.formLabel(comp, tk, "Absolute mass");
		massWidget = new AmountWidget(comp, tk);
		massWidget.setAmount(elem.mass);
		UI.formLabel(comp, tk, "kg");

		if (elem instanceof Substance) {
			Substance subst = (Substance) elem;

			// CAS
			casText = UI.formText(comp, tk, "CAS number");
			Texts.set(casText, subst.casNumber);
			UI.filler(comp, tk);

			// EC number
			ecText = UI.formText(comp, tk, "EC number");
			Texts.set(ecText, subst.ecNumber);
			UI.filler(comp, tk);

			// GUUID
			guuidText = UI.formText(comp, tk,
					"Data dictionary GUUID");
			Texts.set(guuidText, subst.guid);
			UI.filler(comp, tk);

			// renewable resource
			renewableText = UI.formText(comp, tk,
					"Percentage of renewable resources");
			Texts.set(renewableText, subst.renewable);
			UI.formLabel(comp, tk, "%");

			// recycled content
			recycledText = UI.formText(comp, tk,
					"Percentage of recycled content");
			Texts.set(recycledText, subst.recycled);
			UI.formLabel(comp, tk, "%");

			// recyclable content
			recycableText = UI.formText(comp, tk,
					"Percentage of recyclable content");
			Texts.set(recycableText, subst.recyclable);
			UI.formLabel(comp, tk, "%");

			// add validation
			Arrays.asList(renewableText, recycledText, recycableText)
					.forEach(t -> Texts.validateNumber(t));
		}

		commentText = UI.formMultiText(comp, tk, "Comment");
		Texts.set(commentText, App.s(elem.comment));
		UI.filler(comp, tk);
	}

	@Override
	protected void okPressed() {
		LangString.set(elem.name, nameText.getText(), App.lang());
		elem.massPerc = massPercWidget.getAmount();
		elem.mass = massWidget.getAmount();
		LangString.set(elem.comment, commentText.getText(), App.lang());
		if (elem instanceof Substance) {
			Substance subst = (Substance) elem;
			subst.casNumber = Texts.getString(casText);
			subst.ecNumber = Texts.getString(ecText);
			subst.guid = Texts.getString(guuidText);
			subst.renewable = Texts.getDouble(renewableText);
			subst.recycled = Texts.getDouble(recycledText);
			subst.recyclable = Texts.getDouble(recycableText);
		}
		super.okPressed();
	}

}
