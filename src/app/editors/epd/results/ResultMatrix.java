package app.editors.epd.results;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.processes.epd.EpdValue;

import app.M;
import app.editors.epd.EpdEditor;
import app.util.Tables;
import epd.util.Strings;

class ResultMatrix {

	private final EpdEditor editor;
	private final Process epd;
	private final Composite comp;
	private final FormToolkit tk;
	private TableViewer table;

	ResultMatrix(EpdEditor editor, Composite comp, FormToolkit tk) {
		this.editor = editor;
		this.epd = editor.epd;
		this.comp = comp;
		this.tk = tk;
	}

	private void render(List<EpdModuleEntry> entries) {
		if (table != null) {
			table.getTable().dispose();
		}
		var mods = Mod.allOf(editor.getProfile(), entries);
		var results = EpdIndicatorResult.allOf(epd);

		var columns = new String[3 + mods.length];
		columns[0] = M.Code;
		columns[1] = M.Indicator;
		columns[2] = M.Unit;
		for (int i = 0; i < mods.length; i++) {
			columns[3 + i] = mods[i].key();
		}

		table = Tables.createViewer(comp, columns);

	}


	private record Mod(String module, String scenario) {

		static Mod[] allOf(EpdProfile profile, List<EpdModuleEntry> entries) {
			var index = new HashMap<String, Integer>();
			for (var m : profile.getModules()) {
				index.put(m.getName(), m.getIndex());
			}
			return entries.stream()
					.map(Mod::of)
					.sorted((mod1, mod2) -> {
						var i1 = index.get(mod1.module);
						var i2 = index.get(mod2.module);
						if (i1 != null && i2 != null && !i1.equals(i2)) {
							return i1 - i2;
						}
						if (i1 == null && i2 != null)
							return 1;
						if (i1 != null && i2 == null)
							return -1;
						int c = Strings.compare(mod1.module, mod2.module);
						return c == 0
								? Strings.compare(mod1.scenario, mod2.scenario)
								: c;
					})
					.toArray(Mod[]::new);
		}

		static Mod of(EpdModuleEntry e) {
			return new Mod(e.getModule(), e.getScenario());
		}

		String key() {
			return key(module, scenario);
		}

		static String key(String module, String scenario) {
			if (Strings.nullOrEmpty(module))
				return "?";
			return Strings.notEmpty(scenario)
					? module + " - " + scenario
					: module;
		}

		@Override
		public int hashCode() {
			return key().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof Mod other))
				return false;
			return this.key().equals(other.key());
		}

		private boolean matches(EpdValue value) {
			if (value == null)
				return false;
			return Strings.nullOrEqual(module, value.getModule())
					&& Strings.nullOrEqual(scenario, value.getScenario());
		}
	}
}
