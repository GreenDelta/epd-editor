package app.editors.connection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.util.Colors;
import app.util.Controls;
import app.util.UI;

/// Shows the authentication state of a connection and opens the authentication
/// dialog when the link is clicked.
class AuthenticationLink {

	private final ConnectionEditor editor;
	private final SodaConnection con;
	private ImageHyperlink link;

	AuthenticationLink(ConnectionEditor editor) {
		this.editor = editor;
		this.con = editor.con;
	}

	void render(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, M.Authentication);
		link = tk.createImageHyperlink(comp, SWT.NONE);
		link.setForeground(Colors.linkBlue());
		Controls.onClick(link, _ -> {
			if (AuthenticationDialog.open(con)) {
				setLinkText();
				editor.setDirty();
			}
		});
		setLinkText();
	}

	/// Returns the text that describes the authentication state of the given
	/// connection.
	static String linkText(SodaConnection con) {
		return switch (AuthState.of(con)) {
			case TOKEN -> con.user + " - " + M.AccessToken;
			case PASSWORD -> con.user + " - " + M.Password;
			case NONE -> M.AnonymousAccess;
		};
	}

	private void setLinkText() {
		if (link == null)
			return;
		link.setText(linkText(con));
		link.getParent().pack();
	}
}
