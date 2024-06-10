package app.editors.epd.results;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;

import app.M;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.util.Tables;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;

public class ResultMatrix {

	private final EpdEditor editor;
	private final Process epd;
	private final Composite comp;
	private TableViewer table;

	public ResultMatrix(EpdEditor editor, Composite comp) {
		this.editor = editor;
		this.epd = editor.epd;
		this.comp = comp;
	}

	public void render(List<EpdModuleEntry> entries) {
		if (table != null) {
			table.getTable().dispose();
		}
		var profile = editor.getProfile();
		var mods = Mod.allOf(profile, entries);
		var results = IndicatorResults.of(epd, profile, mods);

		var columns = new String[3 + mods.length];
		columns[0] = M.Code;
		columns[1] = M.Indicator;
		columns[2] = M.Unit;
		for (int i = 0; i < mods.length; i++) {
			columns[3 + i] = mods[i].key();
		}
		table = Tables.createViewer(comp, columns);

		var widths = new double[3 + mods.length];
		widths[0] = 0.05;
		widths[1] = 0.2;
		widths[2] = 0.05;
		for (int i = 0; i < mods.length; i++) {
			widths[3 + i] = 0.7 / mods.length;
		}
		Tables.bindColumnWidths(table, widths);

		// bind modifiers
		var modifiers = new ModifySupport<IndicatorResult>(table);
		for (int i = 0; i < mods.length; i++) {
			var mod = mods[i];
			modifiers.bind(mod.key(), new ValueModifier(mod, i));
		}

		table.setLabelProvider(new ResultLabel());
		table.setInput(results);
		comp.layout();
	}

	private class ValueModifier extends TextCellModifier<IndicatorResult> {

		private final Mod mod;
		private final int idx;

		private ValueModifier(Mod mod, int idx) {
			this.mod = mod;
			this.idx = idx;
		}

		@Override
		protected String getText(IndicatorResult r) {
			if (r == null)
				return "!ERROR!";
			var v = r.getModValueAt(idx);
			return v != null
					? v.toString()
					: "";
		}

		@Override
		protected void setText(IndicatorResult r, String text) {
			if (r == null)
				return;
			Double newVal;
			try {
				newVal = Double.parseDouble(text);
			} catch (Exception e) {
				newVal = null;
			}
			var oldVal = r.getModValueAt(idx);
			if (Objects.equals(newVal, oldVal))
				return;
			r.setValue(mod, idx, newVal);
			editor.setDirty();
		}
	}

	private static class ResultLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 && obj instanceof IndicatorResult r
					? Icon.img(r.indicator().getType())
					: null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof IndicatorResult r))
				return null;
			return switch (col) {
				case 0 -> r.getIndicatorCode();
				case 1 -> r.getIndicatorName();
				case 2 -> r.getIndicatorUnit();
				default -> {
					var val = r.getModValueAt(col - 3);
					yield val != null
							? val.toString()
							: " - ";
				}
			};
		}
	}
}
