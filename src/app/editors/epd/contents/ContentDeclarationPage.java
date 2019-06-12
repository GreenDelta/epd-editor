package app.editors.epd.contents;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import app.editors.epd.EpdEditor;
import app.util.UI;
import epd.model.EpdDataSet;

public class ContentDeclarationPage extends FormPage {

	private final EpdEditor editor;
	private final EpdDataSet dataSet;

	public ContentDeclarationPage(EpdEditor editor) {
		super(editor, "ContentDeclarationPage", "#Content declaration");
		this.editor = editor;
		dataSet = editor.dataSet;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "#Content declaration");
		Composite body = UI.formBody(form, mform.getToolkit());

		ContentTable contTable = new ContentTable();
		contTable.render(tk, body);
		ContentTable packTable = new ContentTable();
		packTable.forPackaging = true;
		packTable.render(tk, body);

		form.reflow(true);
	}

}
