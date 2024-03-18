package app.editors.epd.contents;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.util.Epds;

import app.editors.epd.EpdEditor;
import app.util.UI;

public class ContentDeclarationPage extends FormPage {

	private final EpdContentDeclaration decl;

	private final EpdEditor editor;

	public ContentDeclarationPage(EpdEditor editor) {
		super(editor, "ContentDeclarationPage", "Content declaration");
		this.editor = editor;
		this.decl = Epds.withContentDeclaration(editor.epd);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		var form = UI.formHeader(mform, "Content declaration");
		var body = UI.formBody(form, mform.getToolkit());
		Content.sort(decl.getElements());
		var contTable = new ContentTree(editor, decl);
		contTable.render(tk, body);
		var packTable = new ContentTree(editor, decl);
		packTable.forPackaging = true;
		packTable.render(tk, body);
		form.reflow(true);
	}
}
