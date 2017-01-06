package app.editors.upload;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.io.SodaConnection;

import app.store.Connections;
import app.util.Controls;
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
}
