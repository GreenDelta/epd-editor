package app.editors.connection;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.util.Controls;
import app.util.UI;

class ConnectionPage extends FormPage {

	private final SodaConnection con;
	private final ConnectionEditor editor;
	private FormToolkit tk;

	ConnectionPage(ConnectionEditor editor) {
		super(editor, "ConnectionPage", M.ServerConnection);
		this.editor = editor;
		this.con = editor.con;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.ServerConnection);
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite comp = UI.formSection(body, tk, M.ConnectionData);
		text(comp, "URL", con.url, t -> con.url = t);
		text(comp, M.User, con.user, t -> con.user = t);
		text(comp, M.Password, con.password, t -> con.password = t);
		new DataStockLink(editor).render(comp, tk);
		new DataSection(con).create(body, tk);
		UI.filler(comp);
		Button profileBtn = tk.createButton(comp,
				"#Download EPD profiles", SWT.NONE);
		Controls.onSelect(profileBtn, e -> {
			EpdProfileDownload.runInUI(con.url);
		});
		form.reflow(true);
	}

	private Text text(Composite comp, String label, String initial,
			Consumer<String> fn) {
		Text t = UI.formText(comp, tk, label);
		if (initial != null)
			t.setText(initial);
		t.addModifyListener(e -> {
			fn.accept(t.getText());
			editor.setDirty();
		});
		return t;
	}

}
