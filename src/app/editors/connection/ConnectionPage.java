package app.editors.connection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.io.SodaConnection;

import app.util.UI;

class ConnectionPage extends FormPage {

	private final SodaConnection con;
	private final ConnectionEditor editor;

	ConnectionPage(ConnectionEditor editor) {
		super(editor, "ConnectionPage", "#Connection");
		this.editor = editor;
		this.con = editor.con;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "#Connection");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		form.reflow(true);
	}
}
