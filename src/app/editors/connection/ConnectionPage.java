package app.editors.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.descriptors.DataStock;
import org.openlca.ilcd.descriptors.DataStockList;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.util.Controls;
import app.util.MsgBox;
import app.util.UI;

class ConnectionPage extends FormPage {

	private final SodaConnection con;
	private final ConnectionEditor editor;
	private FormToolkit tk;
	private Combo stockCombo;
	private List<DataStock> dataStocks = new ArrayList<>();

	ConnectionPage(ConnectionEditor editor) {
		super(editor, "ConnectionPage", "#Connection");
		this.editor = editor;
		this.con = editor.con;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "#Connection");
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite comp = UI.formSection(body, tk, "#Connection data");
		text(comp, "URL", con.url, t -> con.url = t);
		text(comp, M.User, con.user, t -> con.user = t);
		text(comp, M.Password, con.password, t -> con.password = t);
		createDataStockCombo(comp);
		new DataStockLink(editor).render(comp, tk);
		new DataSection(con).create(body, tk);
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

	private void createDataStockCombo(Composite parent) {
		UI.formLabel(parent, tk, M.DataStock);
		Composite comp = tk.createComposite(parent);
		UI.gridData(comp, true, false);
		UI.innerGrid(comp, 2);
		stockCombo = new Combo(comp, SWT.NONE);
		UI.gridData(stockCombo, false, false).minimumWidth = 180;
		Controls.onSelect(stockCombo, e -> newDataStoreSelected());
		UI.gridData(stockCombo, true, false);
		Button button = new Button(comp, SWT.NONE);
		button.setText(M.GetFromServer);
		Controls.onSelect(button, e -> loadDataStocks());
		updateStockCombo();
		if (con.dataStockName != null)
			stockCombo.setText(con.dataStockName);
	}

	private void loadDataStocks() {
		try (SodaClient client = new SodaClient(con)) {
			client.connect();
			DataStockList dataStockList = client.getDataStockList();
			dataStocks.clear();
			if (dataStockList != null)
				dataStocks.addAll(dataStockList.dataStocks);
			updateStockCombo();
		} catch (Exception e) {
			MsgBox.error("Network connection failed",
					"Network connection failed with the following exception: "
							+ e.getMessage());
		}
	}

	private void updateStockCombo() {
		String[] items = new String[dataStocks.size() + 1];
		items[0] = "";
		int selectedItem = -1;
		for (int i = 0; i < dataStocks.size(); i++) {
			DataStock dataStock = dataStocks.get(i);
			String shortName = dataStock.shortName;
			items[i + 1] = shortName;
			if (Objects.equals(dataStock.uuid, con.dataStockId))
				selectedItem = i + 1;
		}
		stockCombo.setItems(items);
		if (selectedItem != -1)
			stockCombo.select(selectedItem);
	}

	private void newDataStoreSelected() {
		int idx = stockCombo.getSelectionIndex();
		if (idx < 1) {
			con.dataStockName = null;
			con.dataStockId = null;
			return;
		}
		if (idx > dataStocks.size())
			return;
		DataStock stock = dataStocks.get(idx - 1);
		con.dataStockName = stock.shortName;
		con.dataStockId = stock.uuid;
		editor.setDirty();
	}

}
