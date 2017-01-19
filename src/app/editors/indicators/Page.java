package app.editors.indicators;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import app.util.UI;

class Page extends FormPage {

	private IndicatorMappingEditor editor;

	Page(IndicatorMappingEditor editor) {
		super(editor, "Page", "#Indicator mappings");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform,
				"#Indicator mappings");
		Composite body = UI.formBody(form, mform.getToolkit());
		createTable(body, tk);
		form.reflow(true);
	}

	private void createTable(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk,
				"#Indicator mappings");
		UI.gridData(section, true, true);
		// new Table(editor, section, toolkit);
	}
}
