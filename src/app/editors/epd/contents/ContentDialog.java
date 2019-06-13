package app.editors.epd.contents;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.App;
import app.M;
import app.util.Colors;
import app.util.UI;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.model.content.Substance;
import epd.util.Strings;

class ContentDialog extends FormDialog {

	private final ContentElement elem;

	// UI components
	private Text nameText;
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
		ContentDialog cd = new ContentDialog(elem);
		int code = cd.open();
		if (code != OK)
			return code;
		Content.remove(decl, elem);
		// TODO: check parent
		decl.content.add(elem);
		return OK;
	}

	ContentDialog(ContentElement elem) {
		super(UI.shell());
		this.elem = elem;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {

		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(mform.getForm(), tk);

		Composite comp = UI.formComposite(body, tk);
		UI.gridData(comp, true, false);
		UI.gridLayout(comp, 3);

		nameText = UI.formText(comp, tk, M.Name);
		UI.gridData(nameText, true, false).minimumWidth = 250;
		UI.filler(comp, tk);

		UI.formText(comp, tk, "Weight percentage");
		UI.formLabel(comp, tk, "%");

		UI.formText(comp, tk, "Absolute mass");
		UI.formLabel(comp, tk, "%");

		if (elem instanceof Substance) {
			casText = UI.formText(comp, tk, "CAS number");
			UI.filler(comp, tk);
			ecText = UI.formText(comp, tk, "EC number");
			UI.filler(comp, tk);
			guuidText = UI.formText(comp, tk,
					"Data dictionary GUUID");
			UI.filler(comp, tk);

			renewableText = UI.formText(comp, tk,
					"Percentage of renewable resources");
			UI.formLabel(comp, tk, "%");
			recycledText = UI.formText(comp, tk,
					"Percentage of recycled content");
			UI.formLabel(comp, tk, "%");
			recycableText = UI.formText(comp, tk,
					"Percentage of recyclable content");

			Arrays.asList(renewableText, recycledText, recycableText)
					.forEach(t -> validateNumber(t));

			UI.formLabel(comp, tk, "%");
		}

		commentText = UI.formMultiText(comp, tk, "Comment");
		UI.filler(comp, tk);
	}

	@Override
	protected void okPressed() {
		LangString.set(elem.name, nameText.getText(), App.lang());
		// TODO bind mass fields
		LangString.set(elem.comment, commentText.getText(), App.lang());
		if (elem instanceof Substance) {
			Substance subst = (Substance) elem;
			subst.casNumber = str(casText);
			subst.ecNumber = str(ecText);
			subst.guid = str(guuidText);
			subst.renewable = f64(renewableText);
			subst.recycled = f64(recycledText);
			subst.recyclable = f64(recycableText);
		}
		super.okPressed();
	}

	private String str(Text text) {
		if (text == null)
			return null;
		String s = text.getText();
		return Strings.nullOrEmpty(s) ? null : s.trim();
	}

	private Double f64(Text text) {
		String s = str(text);
		if (s == null)
			return null;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return null;
		}
	}

	private void validateNumber(Text t) {
		if (t == null)
			return;
		t.addModifyListener(_e -> {
			String s = t.getText();
			if (Strings.nullOrEmpty(s)) {
				t.setBackground(Colors.white());
				t.setToolTipText(null);
				return;
			}
			try {
				Double.parseDouble(s);
				t.setBackground(Colors.white());
				t.setToolTipText(null);
			} catch (Exception e) {
				t.setBackground(Colors.errorColor());
				t.setToolTipText(s + "is not a number");
			}
		});
	}
}
