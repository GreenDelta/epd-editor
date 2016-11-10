package app.editors.epd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import app.M;
import app.rcp.Labels;
import app.util.Tables;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.IndicatorResult;

class ModuleResultTable {

	private EpdEditor editor;
	private EpdDataSet dataSet;

	private TableViewer viewer;

	public ModuleResultTable(EpdEditor editor, EpdDataSet dataSet) {
		this.editor = editor;
		this.dataSet = dataSet;
	}

	public void create(Composite composite) {
		String[] columns = new String[] { M.Module, M.Scenario,
				M.Indicator, M.Value, M.Unit };
		viewer = Tables.createViewer(composite, columns);
		Tables.bindColumnWidths(viewer, 0.1, 0.2, 0.3, 0.2, 0.2);
		ModifySupport<ResultRow> modifySupport = new ModifySupport<>(viewer);
		modifySupport.bind(M.Value, new AmountModifier());
		ResultLabel label = new ResultLabel();
		viewer.setLabelProvider(label);
		// TODO: Viewers.sortByLabels(viewer, label, 0, 1, 2, 4);
	}

	public void refresh() {
		List<ResultRow> rows = new ArrayList<>();
		for (IndicatorResult result : dataSet.results) {
			for (Amount amount : result.amounts) {
				ResultRow row = new ResultRow();
				row.amount = amount;
				row.result = result;
				rows.add(row);
			}
		}
		viewer.setInput(rows);
	}

	private class ResultRow {
		IndicatorResult result;
		Amount amount;
	}

	private class ResultLabel extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof ResultRow))
				return null;
			ResultRow row = (ResultRow) element;
			Amount a = row.amount;
			switch (col) {
			case 0:
				return a.module == null ? null : a.module.getLabel();
			case 1:
				return a.scenario;
			case 2:
				return Labels.get(row.result.indicator);
			case 3:
				return a.value == null ? " - " : a.value.toString();
			case 4:
				return row.result.indicator.getUnit();
			default:
				return null;
			}
		}
	}

	private class AmountModifier extends TextCellModifier<ResultRow> {

		@Override
		protected String getText(ResultRow row) {
			Double val = row.amount.value;
			return val == null ? " - " : val.toString();
		}

		@Override
		protected void setText(ResultRow row, String text) {
			Double newVal;
			try {
				newVal = Double.parseDouble(text);
			} catch (Exception e) {
				newVal = null;
			}
			if (Objects.equals(newVal, row.amount.value))
				return;
			row.amount.value = newVal;
			editor.setDirty();
		}
	}

}
