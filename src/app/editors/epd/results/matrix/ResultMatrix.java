package app.editors.epd.results.matrix;

import java.util.List;

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
		widths[0] = 0.1;
		widths[1] = 0.3;
		widths[2] = 0.1;
		for (int i = 0; i < mods.length; i++) {
			widths[3 + i] = 0.5 / mods.length;
		}
		Tables.bindColumnWidths(table, widths);

		table.setLabelProvider(new ResultLabel());
		table.setInput(results);
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
