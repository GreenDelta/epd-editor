package app.editors.epd.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.processes.epd.EpdValue;
import org.openlca.ilcd.util.EpdIndicatorResult;
import org.openlca.ilcd.util.Epds;

import epd.profiles.EpdProfile;
import epd.profiles.EpdProfiles;
import epd.profiles.Indicator;
import epd.util.Strings;

/**
 * Initializes indicator results for added modules and removes results for
 * module entries that no longer exist in an EPD data set.
 */
class ResultSync implements Runnable {

	private final Process epd;
	private final EpdProfile profile;

	public ResultSync(Process epd) {
		this.epd = epd;
		this.profile = EpdProfiles.get(epd);
	}

	@Override
	public void run() {

		// collect the defined module-scenario pairs
		var definedMods = Epds.getModuleEntries(epd)
			.stream()
			.map(e -> e.getModule() + "/" + e.getScenario())
			.collect(Collectors.toSet());
		if (definedMods.isEmpty()) {
			EpdIndicatorResult.clear(epd);
			return;
		}

		var results = new ArrayList<>(EpdIndicatorResult.allOf(epd));

		// remove the results with non-matching or duplicate indicators
		var index = cleanResults(results);

		// add new result entries
		for (var indicator : profile.getIndicators()) {

			// get or create the result
			var result = index.get(indicator.getUUID());
			if (result == null) {
				result = indicator.createResult();
				results.add(result);
			}

			// remove & add amounts
			removeAmounts(definedMods, result);
			for (var entry : Epds.getModuleEntries(epd)) {
				var amount = findValue(result, entry);
				if (amount != null)
					continue;
				amount = new EpdValue()
					.withModule(entry.getModule())
					.withScenario(entry.getScenario());
				result.values().add(amount);
			}
		}

		EpdIndicatorResult.writeClean(epd, results);
	}

	private Map<String, EpdIndicatorResult> cleanResults(
		List<EpdIndicatorResult> results
	) {

		var indicatorIds = profile.getIndicators().stream()
			.map(Indicator::getUUID)
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
		var handled = new HashMap<String, EpdValue>();
		var removals = new ArrayList<EpdValue>();
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

	private EpdValue findValue(EpdIndicatorResult result, EpdModuleEntry entry) {
		for (var v : result.values()) {
			if (Objects.equals(entry.getModule(), v.getModule())
				&& Strings.nullOrEqual(entry.getScenario(), v.getScenario()))
				return v;
		}
		return null;
	}

}
