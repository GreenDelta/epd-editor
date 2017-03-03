package app.editors.io;

import java.util.Objects;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;

import app.store.Connections;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

class ConnectionCombo {

	SodaConnection selected;

	private final SodaConnection[] cons;
	private Combo combo;

	static ConnectionCombo create(Composite parent) {
		ConnectionCombo cc = new ConnectionCombo();
		cc.render(parent);
		return cc;
	}

	private ConnectionCombo() {
		cons = Connections.get().toArray(new SodaConnection[0]);
	}

	private void render(Composite comp) {
		combo = UI.formCombo(comp, "#Connection");
		for (SodaConnection con : cons) {
			combo.add(con.toString());
		}
		if (cons.length > 0) {
			combo.select(0);
			selected = cons[0];
		}
		Controls.onSelect(combo, e -> {
			selected = cons[combo.getSelectionIndex()];
		});
	}

	void select(SodaConnection con) {
		if (con == null)
			return;
		int idx = -1;
		for (int i = 0; i < cons.length; i++) {
			if (Objects.equals(cons[i], con)) {
				idx = i;
				break;
			}
		}
		if (idx > -1) {
			selected = con;
			combo.select(idx);
		}
	}

	SodaClient makeClient() {
		SodaConnection con = selected;
		if (con == null) {
			MsgBox.error("#No connection",
					"#There is no connection selected");
			return null;
		}
		try {
			SodaClient client = new SodaClient(con);
			client.connect();
			return client;
		} catch (Exception e) {
			MsgBox.error("#Connection failed",
					"#Connection to client failed: " + e.getMessage());
			return null;
		}
	}
}
