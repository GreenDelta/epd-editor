package app.editors.connection;

import app.M;
import app.rcp.Icon;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import epd.util.Strings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.descriptors.DataStock;

import java.util.List;
import java.util.Objects;

class DataStockDialog extends FormDialog {

	private final List<DataStock> dataStocks;
	DataStock selected;

	DataStockDialog(List<DataStock> dataStocks) {
		super(UI.shell());
		this.dataStocks = dataStocks;
		dataStocks.sort((s1, s2) -> Strings.compare(s1.shortName, s2.shortName));
		setBlockOnOpen(true);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		UI.formHeader(mform, M.DataStocks);
		Composite body = UI.formBody(mform.getForm(), tk);
		UI.gridLayout(body, 1);
		TableViewer table = Tables.createViewer(body, M.Name, M.UUID,
			M.Description);
		table.setLabelProvider(new Label());
		tk.adapt(table.getTable());
		Tables.bindColumnWidths(table, 0.2, 0.2, 0.6);
		table.setInput(dataStocks);
		table.addSelectionChangedListener(e -> {
			selected = Viewers.getFirstSelected(table);
			getButton(IDialogConstants.OK_ID).setEnabled(selected != null);
			table.refresh();
		});
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return c;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof DataStock stock))
				return null;
			if (col != 0)
				return null;
			if (Objects.equals(stock, selected))
				return Icon.CHECK_TRUE.img();
			else
				return Icon.CHECK_FALSE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof DataStock stock))
				return null;
			return switch (col) {
				case 0 -> stock.shortName;
				case 1 -> stock.uuid;
				case 2 -> stock.description == null ? null
					: stock.description.value;
				default -> null;
			};
		}
	}
}
