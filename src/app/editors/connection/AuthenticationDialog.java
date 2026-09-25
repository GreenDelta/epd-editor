package app.editors.connection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.commons.Strings;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;

import app.App;
import app.M;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

/// A dialog for editing the authentication information of a connection. The
/// password is only used for a session based login or for generating a new
/// token; it is not stored when a token is used.
class AuthenticationDialog extends FormDialog {

	private final SodaConnection con;
	private Text userText;
	private Text passwordText;
	private Text tokenText;

	/// Opens the dialog and returns true when it was closed with OK.
	static boolean open(SodaConnection con) {
		if (con == null)
			return false;
		return new AuthenticationDialog(con).open() == OK;
	}

	private AuthenticationDialog(SodaConnection con) {
		super(UI.shell());
		this.con = con;
		setBlockOnOpen(true);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(M.Authentication);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(700, 450);
	}

	@Override
	protected void createFormContent(IManagedForm form) {
		var tk = form.getToolkit();
		UI.formHeader(form, M.AuthDialogTitle);
		var body = UI.formBody(form.getForm(), tk);
		UI.gridLayout(body, 1);

		var info = tk.createLabel(body, M.AuthDialogMessage, SWT.WRAP);
		UI.stretchX(info);

		var comp = UI.formComposite(body, tk);
		UI.stretchX(comp);
		UI.gridLayout(comp, 3, 10, 0);

		userText = UI.formText(comp, tk, M.User);
		if (con.user != null) {
			userText.setText(con.user);
		}
		UI.filler(comp, tk);

		UI.formLabel(comp, tk, M.Password);
		passwordText = tk.createText(comp, "", SWT.PASSWORD | SWT.BORDER);
		UI.stretchX(passwordText);
		UI.filler(comp, tk);

		// the token field has an additional button
		tokenText = UI.formText(comp, tk, M.Token);
		UI.stretchX(tokenText);
		if (con.token != null) {
			tokenText.setText(con.token);
		}
		var genBtn = tk.createButton(comp, M.Generate, SWT.PUSH);
		genBtn.addSelectionListener(Controls.onSelect(_ -> generateToken()));
	}

	@Override
	protected void okPressed() {
		var user = text(userText);
		var pw = text(passwordText);
		var token = text(tokenText);

		if (Strings.isNotBlank(token)) {
			if (Strings.isBlank(user)) {
				MsgBox.error(M.Authentication, M.UserNameRequired);
				return;
			}
			con.user = user;
			con.token = token;
			con.password = null;
		} else if (Strings.isNotBlank(pw)) {
			if (Strings.isBlank(user)) {
				MsgBox.error(M.Authentication, M.UserNameRequired);
				return;
			}
			con.user = user;
			con.password = pw;
			con.token = null;
		} else {
			// anonymous access
			con.user = null;
			con.password = null;
			con.token = null;
		}
		super.okPressed();
	}

	/// Generates a new token with the entered user name and password.
	private void generateToken() {
		var user = text(userText);
		var pw = text(passwordText);
		if (Strings.isBlank(user) || Strings.isBlank(pw)) {
			MsgBox.error(M.Authentication, M.UserNameAndPasswordRequired);
			return;
		}
		var res = App.exec(M.GenerateToken, () -> {
			try (var client = SodaClient.of(con.url)) {
				return client.getAuthenticationToken(user, pw);
			}
		});
		if (res == null || res.isError()) {
			MsgBox.error(M.FailedToGenerateToken,
				res == null ? "unknown error" : res.error());
			return;
		}
		tokenText.setText(res.value());

		// the password is only needed for generating the token
		passwordText.setText("");
	}

	private String text(Text text) {
		return text == null ? null : text.getText().strip();
	}
}
