package app.editors.matprops;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.util.UI;
import epd.model.MaterialProperty;

class Page extends FormPage {

	private FormToolkit toolkit;
	private MaterialPropertyEditor editor;
	private List<MaterialProperty> properties;

	public Page(MaterialPropertyEditor editor,
			List<MaterialProperty> properties) {
		super(editor, "MaterialPropertyPage", M.MaterialProperties);
		this.editor = editor;
		this.properties = properties;
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
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		Table viewer = new Table(editor,
				comp);
		viewer.setInput(properties);
		viewer.bindTo(section);
	}

}