package app.editors.epd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import app.M;
import app.util.Tables;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.Indicator;
import epd.model.IndicatorResult;
import epd.model.Module;
import epd.util.Strings;

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
		ModifySupport<ResultRow> modifier = new ModifySupport<>(viewer);
		modifier.bind(M.Value, new AmountModifier());
		ResultLabel label = new ResultLabel();
		viewer.setLabelProvider(label);
		Tables.addSorter(viewer, 0, (ResultRow r) -> r.amount.module);
		Tables.addSorter(viewer, 1, (ResultRow r) -> r.amount.scenario);
		Tables.addSorter(viewer, 2, (ResultRow r) -> r.result.indicator.name);
		Tables.addSorter(viewer, 3, (ResultRow r) -> r.amount.value);
		Tables.addSorter(viewer, 4, (ResultRow r) -> r.result.indicator.unit);
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
		Collections.sort(rows);
		viewer.setInput(rows);
	}

	private class ResultRow implements Comparable<ResultRow> {
		IndicatorResult result;
		Amount amount;

		@Override
		public int compareTo(ResultRow other) {
			if (other == null)
				return 1;
			if (this.amount == null || other.amount == null)
				return 0;

			// compare by modules
			Module m1 = this.amount.module;
			Module m2 = other.amount.module;
			if (m1 == null || m2 == null)
				return 0;
			int c = m1.compareTo(m2);
			if (c != 0)
				return c;

			// compare by scenarios
			String s1 = this.amount.scenario;
			String s2 = other.amount.scenario;
			c = Strings.compare(s1, s2);
			if (c != 0)
				return c;

			// compare by indicators
			if (this.result == null || other.result == null)
				return 0;
			Indicator i1 = this.result.indicator;
			Indicator i2 = other.result.indicator;
			if (i1 == null || i2 == null)
				return 0;
			return Strings.compare(i1.name, i2.name);
		}
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
				return a.module == null ? null : a.module.name;
			case 1:
				return a.scenario;
			case 2:
				return row.result.indicator.name;
			case 3:
				return a.value == null ? " - " : a.value.toString();
			case 4:
				return row.result.indicator.unit;
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
