package app.editors.epd.contents;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.editors.epd.EpdEditor;
import app.util.UI;
import epd.model.EpdDataSet;
import epd.model.content.ContentDeclaration;

public class ContentDeclarationPage extends FormPage {

	private final ContentDeclaration decl;

	private final EpdEditor editor;

	public ContentDeclarationPage(EpdEditor editor) {
		super(editor, "ContentDeclarationPage", "#Content declaration");
		this.editor = editor;
		EpdDataSet ds = editor.dataSet;
		if (ds.contentDeclaration != null) {
			decl = ds.contentDeclaration;
		} else {
			decl = new ContentDeclaration();
			ds.contentDeclaration = decl;
		}
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "#Content declaration");
		Composite body = UI.formBody(form, mform.getToolkit());
		Content.sort(decl.content);
		ContentTree contTable = new ContentTree(editor, decl);
		contTable.render(tk, body);
		ContentTree packTable = new ContentTree(editor, decl);
		packTable.forPackaging = true;
		packTable.render(tk, body);
		form.reflow(true);
	}
}
