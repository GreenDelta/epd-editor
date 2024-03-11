package app.editors.epd;

import app.App;
import app.store.EpdProfiles;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;
import epd.model.ModuleEntry;
import epd.util.Strings;
import org.openlca.ilcd.processes.epd.EpdResult;
import org.openlca.ilcd.util.EpdIndicatorResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Initializes indicator results for added modules and removes results for
 * module entries that no longer exist in an EPD data set.
 */
class ResultSync implements Runnable {

	private final EpdDataSet epd;
	private final EpdProfile profile;

	public ResultSync(EpdDataSet epd) {
		this.epd = epd;
		this.profile = EpdProfiles.get(epd.process);
	}

	@Override
	public void run() {

		// collect the defined module-scenario pairs
		var definedMods = epd.moduleEntries.stream()
			.map(e -> e.module + "/" + e.scenario)
			.collect(Collectors.toSet());
		if (definedMods.isEmpty()) {
			EpdIndicatorResult.clear(epd.process);
			return;
		}

		var results = new ArrayList<>(EpdIndicatorResult.allOf(epd.process));

		// remove the results with non-matching or duplicate indicators
		var index = cleanResults(results);

		// add new result entries
		for (var indicator : profile.indicators) {

			// get or create the result
			var result = index.get(indicator.uuid);
			if (result == null) {
				result = indicator.createResult(App.lang());
				results.add(result);
			}

			// remove & add amounts
			removeAmounts(definedMods, result);
			for (var entry : epd.moduleEntries) {
				var amount = findAmount(result, entry);
				if (amount != null)
					continue;
				amount = new EpdResult()
					.withModule(entry.module.name)
					.withScenario(entry.scenario);
				result.values().add(amount);
			}
		}

		EpdIndicatorResult.writeClean(epd.process, results);
	}

	private Map<String, EpdIndicatorResult> cleanResults(
		List<EpdIndicatorResult> results
	) {

		var indicatorIds = profile.indicators.stream()
			.map(indicator -> indicator.uuid)
			.collect(Collectors.toSet());

		var removals = new ArrayList<EpdIndicatorResult>();
		var handled = new HashMap<String, EpdIndicatorResult>();
		for (var r : results) {

			if (r.indicator() == null
				|| r.indicator().getUUID() == null
				|| !indicatorIds.contains(r.indicator().getUUID())) {
				removals.add(r);
				continue;
			}

			var id = r.indicator().getUUID();
			var dup = handled.get(id);
			if (dup == null) {
				handled.put(id, r);
				continue;
			}

			// in case of duplicates try to keep the result with
			// the highest values
			var ar = r.values().stream().mapToDouble(
				a -> a.getAmount() == null ? 0 : a.getAmount()).sum();
			var adup = dup.values().stream().mapToDouble(
				a -> a.getAmount() == null ? 0 : a.getAmount()).sum();
			if (ar <= adup) {
				removals.add(r);
			} else {
				removals.add(dup);
				handled.put(id, r);
			}
		}
		results.removeAll(removals);
		return handled;
	}

	/**
	 * Remove outdated amount entries and duplicates.
	 */
	private void removeAmounts(Set<String> definedMods, EpdIndicatorResult result) {
		var handled = new HashMap<String, EpdResult>();
		var removals = new ArrayList<EpdResult>();
		for (var a : result.values()) {
			var key = a.getModule() + "/" + a.getScenario();
			if (!definedMods.contains(key)) {
				removals.add(a);
				continue;
			}
			var dup = handled.get(key);
			if (dup == null) {
				handled.put(key, a);
				continue;
			}

			// try to keep the higher value in case of a duplicate
			if (a.getAmount() == null
				|| (dup.getAmount() != null && dup.getAmount() > a.getAmount())) {
				removals.add(a);
			} else if (dup.getAmount() == null || a.getAmount() > dup.getAmount()) {
				removals.add(dup);
				handled.put(key, a);
			} else {
				removals.add(a);
			}
		}
		result.values().removeAll(removals);
	}

	private EpdResult findAmount(EpdIndicatorResult result, ModuleEntry entry) {
		for (var amount : result.values()) {
			if (Objects.equals(entry.module.name, amount.getModule())
				&& Strings.nullOrEqual(entry.scenario, amount.getScenario()))
				return amount;
		}
		return null;
	}

}
