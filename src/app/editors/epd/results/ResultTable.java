package app.editors.epd.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdValue;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.Editors;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.util.Tables;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.profiles.EpdProfiles;
import epd.util.Strings;

class ResultTable {

	private final EpdEditor editor;
	private final Process epd;

	private TableViewer table;

	public ResultTable(EpdEditor editor, Process epd) {
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
			var ref = row.result.indicator();
			var indexRef = App.index().find(ref);
			if (indexRef != null) {
				Editors.open(indexRef);
			}
		});

		// add sorters
		Tables.addSorter(table, 0, ResultRow::module);
		Tables.addSorter(table, 1, ResultRow::scenario);
		Tables.addSorter(table, 2, ResultRow::indicator);
		Tables.addSorter(table, 3, ResultRow::amount);
		Tables.addSorter(table, 4, ResultRow::unit);
	}

	public void refresh() {
		List<ResultRow> rows = new ArrayList<>();
		for (var result : EpdProfiles.syncResultsOf(epd)) {
			for (var amount : result.values()) {
				rows.add(new ResultRow(result, amount));
			}
		}
		Collections.sort(rows);
		table.setInput(rows);
	}

	private record ResultRow(
		EpdIndicatorResult result,
		EpdValue value
	) implements Comparable<ResultRow> {

		String module() {
			return value.getModule();
		}

		String scenario() {
			return value.getScenario();
		}

		String indicator() {
			return result.indicator() != null
				? App.s(result.indicator().getName())
				: null;
		}

		String unit() {
			return result.unitGroup() != null
				? App.s(result.unitGroup().getName())
				: null;
		}

		Double amount() {
			return value.getAmount();
		}

		@Override
		public int compareTo(ResultRow other) {
			if (other == null)
				return 1;

			// compare by modules
			int c = Strings.compare(this.module(), other.module());
			if (c != 0)
				return c;

			// compare by scenarios
			c = Strings.compare(this.scenario(), other.scenario());
			if (c != 0)
				return c;

			// compare by indicators
			return Strings.compare(this.indicator(), other.indicator());
		}
	}

	private static class ResultLabel extends LabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof ResultRow row))
				return null;
			if (col == 2) {
				var r = row.result();
				if (r == null)
					return null;
				return r.hasInventoryIndicator()
					? Icon.img(DataSetType.FLOW)
					: Icon.img(DataSetType.IMPACT_METHOD);
			}

			if (col == 4)
				return Icon.img(DataSetType.UNIT_GROUP);
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ResultRow row))
				return null;
			return switch (col) {
				case 0 -> row.module();
				case 1 -> row.scenario();
				case 2 -> row.indicator();
				case 3 -> row.amount() == null ? " - " : row.amount().toString();
				case 4 -> row.unit();
				default -> null;
			};
		}
	}

	private class AmountModifier extends TextCellModifier<ResultRow> {

		@Override
		protected String getText(ResultRow row) {
			var val = row.amount();
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
			if (Objects.equals(newVal, row.amount()))
				return;
			row.value.withAmount(newVal);
			editor.setDirty();
		}
	}

}
