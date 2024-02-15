package app.editors.epd;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.Editors;
import app.rcp.Icon;
import app.util.Tables;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.Indicator;
import epd.model.IndicatorResult;
import epd.model.Module;
import epd.util.Strings;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.DataSetType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class ResultTable {

	private final EpdEditor editor;
	private final EpdDataSet epd;

	private TableViewer table;

	public ResultTable(EpdEditor editor, EpdDataSet epd) {
		this.editor = editor;
		this.epd = epd;
	}

	public void create(Composite composite) {
		var columns = new String[]{
			M.Module, M.Scenario, M.Indicator, M.Value, M.Unit};
		table = Tables.createViewer(composite, columns);
		Tables.bindColumnWidths(table, 0.1, 0.2, 0.3, 0.2, 0.2);
		var modifier = new ModifySupport<ResultRow>(table);
		modifier.bind(M.Value, new AmountModifier());
		table.setLabelProvider(new ResultLabel());
		table.getTable().setToolTipText(Tooltips.EPD_Results);

		Tables.onDoubleClick(table, $ -> {
			ResultRow row = Viewers.getFirstSelected(table);
			if (row == null)
				return;
			var indicator = row.indicator();
			if (indicator == null)
				return;
			var ref = row.indicator().getRef(App.lang());
			var indexRef = App.index().find(ref);
			if (indexRef != null) {
				Editors.open(indexRef);
			}
		});

		// add sorters
		Tables.addSorter(table, 0, (ResultRow r) -> r.amount.module);
		Tables.addSorter(table, 1, (ResultRow r) -> r.amount.scenario);
		Tables.addSorter(table, 2, (ResultRow r) -> r.result.indicator.name);
		Tables.addSorter(table, 3, (ResultRow r) -> r.amount.value);
		Tables.addSorter(table, 4, (ResultRow r) -> r.result.indicator.unit);
	}

	public void refresh() {
		List<ResultRow> rows = new ArrayList<>();
		for (IndicatorResult result : epd.results) {
			for (Amount amount : result.amounts) {
				rows.add(new ResultRow(result, amount));
			}
		}
		Collections.sort(rows);
		table.setInput(rows);
	}

	private static class ResultRow implements Comparable<ResultRow> {

		private final IndicatorResult result;
		private final Amount amount;

		ResultRow(IndicatorResult result, Amount amount) {
			this.result = result;
			this.amount = amount;
		}

		Indicator indicator() {
			return result.indicator;
		}

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

	private static class ResultLabel extends LabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof ResultRow row))
				return null;
			if (col == 2) {
				var indicator = row.indicator();
				if (indicator == null)
					return null;
				return indicator.type == Indicator.Type.LCI
					? Icon.img(DataSetType.FLOW)
					: Icon.img(DataSetType.LCIA_METHOD);
			}

			if (col == 4)
				return Icon.img(DataSetType.UNIT_GROUP);
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ResultRow row))
				return null;
			var a = row.amount;
			return switch (col) {
				case 0 -> a.module == null ? null : a.module.name;
				case 1 -> a.scenario;
				case 2 -> row.result.indicator.name;
				case 3 -> a.value == null ? " - " : a.value.toString();
				case 4 -> row.result.indicator.unit;
				default -> null;
			};
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
