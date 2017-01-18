package app.editors.connection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.openlca.ilcd.descriptors.DataStock;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;

import app.App;
import app.M;
import app.util.Colors;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

class DataStockLink {

	private final ConnectionEditor editor;
	private final SodaConnection con;
	private ImageHyperlink link;

	DataStockLink(ConnectionEditor editor) {
		this.editor = editor;
		this.con = editor.con;
	}

	void render(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, M.DataStock);
		link = tk.createImageHyperlink(comp, SWT.NONE);
		link.setForeground(Colors.linkBlue());
		Controls.onClick(link, e -> {
			String[] error = new String[1];
			List<DataStock> list = new ArrayList<>();
			App.run("Get data stocks...", () -> {
				fetchStocks(list, error);
			}, () -> {
				afterFetch(list, error);
			});

		});
		setLinkText();
	}

	private void fetchStocks(List<DataStock> list, String[] error) {
		try (SodaClient client = new SodaClient(con)) {
			client.connect();
			list.addAll(client.getDataStockList().dataStocks);
		} catch (Exception ex) {
			error[0] = ex.getMessage();
		}
	}

	private void afterFetch(List<DataStock> list, String[] error) {
		if (error[0] != null) {
			MsgBox.error("#Failed to get data stocks", error[0]);
			return;
		}
		DataStockDialog dialog = new DataStockDialog(list);
		if (dialog.open() != Window.OK || dialog.selected == null)
			return;
		con.dataStockId = dialog.selected.uuid;
		con.dataStockName = dialog.selected.shortName;
		setLinkText();
		editor.setDirty();
	}

	private void setLinkText() {
		if (link == null)
			return;
		String t = "#none";
		if (con.dataStockId != null) {
			t = con.dataStockName + " | " + con.dataStockId;
		}
		link.setText(t);
	}
}
