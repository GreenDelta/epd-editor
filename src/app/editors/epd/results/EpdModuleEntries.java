package app.editors.epd.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Strings;

public final class EpdModuleEntries {

	private EpdModuleEntries() {
	}

	/**
	 * Returns a life-list of the declared modules of the given EPD. This method
	 * also adds modules to this list, which are not declared yet but used in the
	 * results of the EPD.
	 */
	public static List<EpdModuleEntry> withAllOf(Process epd) {
		if (epd == null)
			return new ArrayList<>();

		// collect declared modules
		var modules = Epds.withModuleEntries(epd);
		var modKeys = new HashSet<String>();
		BiFunction<String, String, Boolean> modFn = (mod, scen) -> {
			var key = Strings.notEmpty(scen)
					? mod + "/" + scen
					: mod;
			return modKeys.add(key);
		};
		modules.forEach(m -> modFn.apply(m.getModule(), m.getScenario()));

		// add module entries
		for (var r : EpdIndicatorResult.allOf(epd)) {
			for (var v : r.values()) {
				if (modFn.apply(v.getModule(), v.getScenario())) {
					var mod = new EpdModuleEntry()
							.withModule(v.getModule())
							.withScenario(v.getScenario());
					modules.add(mod);
				}
			}
		}

		// sort the module entries
		modules.sort((e1, e2) -> {
			int c = Strings.compare(e1.getModule(), e2.getModule());
			return c == 0
					? Strings.compare(e1.getScenario(), e2.getScenario())
					: c;
		});

		return modules;
	}
}
