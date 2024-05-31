package app.editors.epd.results.matrix;

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
import org.openlca.ilcd.processes.epd.EpdModuleEntry;

import app.App;
import app.M;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.util.Tables;
import epd.util.Strings;

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
		var mods = Mod.allOf(editor.getProfile(), entries);
		var results = EpdIndicatorResult.allOf(epd);
		results.sort((r1, r2) -> {
			if (r1.indicator() == null)
				return 0;
			if (r2.indicator() == null)
				return 0;
			if (r1.hasImpactIndicator() != r2.hasImpactIndicator())
				return r1.hasImpactIndicator() ? -1 : 1;
			var n1 = App.s(r1.indicator());
			var n2 = App.s(r2.indicator());
			return Strings.compare(n1, n2);
		});

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

		table.setLabelProvider(new ResultLabel(mods));
		table.setInput(results);
	}

	private class ResultLabel extends LabelProvider
			implements ITableLabelProvider {

		private final Mod[] mods;

		ResultLabel(Mod[] mods) {
			this.mods = mods;
		}

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (col != 0 || !(obj instanceof EpdIndicatorResult r))
				return null;
			return r.hasImpactIndicator()
					? Icon.img(DataSetType.IMPACT_METHOD)
					: Icon.img(DataSetType.FLOW);
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdIndicatorResult r))
				return null;
			return switch (col) {
				case 0 -> codeOf(r);
				case 1 -> App.s(r.indicator());
				case 2 -> App.s(r.unitGroup());
				default -> valOf(r, col - 3);
			};
		}

		private String codeOf(EpdIndicatorResult r) {
			if (r == null || r.indicator() == null)
				return "";
			var profile = editor.getProfile();
			if (profile == null)
				return "";
			for (var i : profile.getIndicators()) {
				if (Objects.equals(i.getUUID(), r.indicator().getUUID()))
					return i.getCode();
			}
			return "";
		}

		private String valOf(EpdIndicatorResult r, int idx) {
			if (r == null
					|| r.values() == null
					|| idx < 0
					|| idx >= mods.length)
				return " - ";
			var mod = mods[idx];
			var val = r.values().stream()
					.filter(mod::matches)
					.findAny()
					.orElse(null);
			return val != null && val.getAmount() != null
					? Double.toString(val.getAmount())
					: " - ";
		}
	}
}
