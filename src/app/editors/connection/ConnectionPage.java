package app.editors.connection;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.util.UI;

class ConnectionPage extends FormPage {

	private final SodaConnection con;
	private final ConnectionEditor editor;

	ConnectionPage(ConnectionEditor editor) {
		super(editor, "ConnectionPage", M.ServerConnection);
		this.editor = editor;
		this.con = editor.con;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var form = UI.formHeader(mform, M.ServerConnection);
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		var comp = UI.formSection(body, tk, M.ConnectionData);

		// URL
		var urlText = UI.formText(comp, tk, "URL");
		if (con.url != null) {
			urlText.setText(con.url);
		}
		urlText.addModifyListener(_ -> {
			con.url = urlText.getText();
			editor.setDirty();
		});

		new AuthenticationLink(editor).render(comp, tk);
		new DataStockLink(editor).render(comp, tk);
		new DataSection(con).create(body, tk);
		form.reflow(true);
	}
}
