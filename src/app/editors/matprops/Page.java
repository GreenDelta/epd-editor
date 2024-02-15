package app.editors.matprops;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.util.UI;

class Page extends FormPage {

	private FormToolkit toolkit;
	private final MaterialPropertyEditor editor;

	public Page(MaterialPropertyEditor editor) {
		super(editor, "MaterialPropertyPage", M.MaterialProperties);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		toolkit = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform,
				M.MaterialProperties);
		Composite body = UI.formBody(form, mform.getToolkit());
		createTable(body);
		form.reflow(true);
	}

	private void createTable(Composite parent) {
		Section section = UI.section(parent, toolkit,
				M.MaterialProperties);
		UI.gridData(section, true, true);
		new Table(editor, section, toolkit);
	}

}
