package app.editors.epd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import app.store.EpdProfiles;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.IndicatorResult;
import epd.model.ModuleEntry;
import epd.util.Strings;

/**
 * Initializes indicator results for added modules and removes results for
 * module entries that no longer exist in an EPD data set.
 */
class ModuleResultSync implements Runnable {

	private final EpdDataSet epd;

	public ModuleResultSync(EpdDataSet epd) {
		this.epd = epd;
	}

	@Override
	public void run() {

		// load the profile
		var profile = Objects.requireNonNullElseGet(
			EpdProfiles.get(epd.profile),
			EpdProfiles::getDefault);


		// remove the results with non-matching or duplicate
		// indicators
		removeResults(profile);

		// add new result entries
		var definedMods = epd.moduleEntries.stream()
			.map(e -> e.module + "/" + e.scenario)
			.collect(Collectors.toSet());
		for (var indicator : profile.indicators) {

			// get or create the result
			var result = epd.getResult(indicator);
			if (result == null) {
				result = new IndicatorResult();
				result.indicator = indicator;
				epd.results.add(result);
			}

			// remove & add amounts
			removeAmounts(definedMods, result);
			for (var entry : epd.moduleEntries) {
				var amount = findAmount(result, entry);
				if (amount != null)
					continue;
				amount = new Amount();
				amount.module = entry.module;
				amount.scenario = entry.scenario;
				result.amounts.add(amount);
			}
		}
	}

	private void removeResults(epd.model.EpdProfile profile) {
		var indicatorIds = profile.indicators.stream()
			.map(indicator -> indicator.uuid)
			.collect(Collectors.toSet());
		var removals = new ArrayList<IndicatorResult>();
		var handled = new HashMap<String, IndicatorResult>();
		for (var r : epd.results) {
			if (r.indicator == null
					|| r.indicator.uuid == null
					|| !indicatorIds.contains(r.indicator.uuid)) {
				removals.add(r);
				continue;
			}
			var id = r.indicator.uuid;
			var dup = handled.get(id);
			if (dup == null) {
				handled.put(id, r);
				continue;
			}

			// in case of duplicates try to keep the result with
			// the highest values
			var ar = r.amounts.stream().mapToDouble(
				a -> a.value == null ? 0 : a.value).sum();
			var adup = dup.amounts.stream().mapToDouble(
				a -> a.value == null ? 0 : a.value).sum();
			if (ar <= adup) {
				removals.add(r);
			} else {
				removals.add(dup);
				handled.put(id, r);
			}
		}
		epd.results.removeAll(removals);
	}

	/**
	 * Remove outdated amount entries and duplicates.
	 */
	private void removeAmounts(Set<String> definedMods, IndicatorResult result) {
		var handled = new HashMap<String, Amount>();
		var removals = new ArrayList<Amount>();
		for (var a : result.amounts) {
			var key = a.module + "/" + a.scenario;
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
			if (a.value == null || (dup.value != null && dup.value > a.value)) {
				removals.add(a);
			} else if (dup.value == null || a.value > dup.value) {
				removals.add(dup);
				handled.put(key, a);
			} else {
				removals.add(a);
			}
		}
		result.amounts.removeAll(removals);
	}

	private Amount findAmount(IndicatorResult result, ModuleEntry entry) {
		for (var amount : result.amounts) {
			if (Objects.equals(entry.module, amount.module)
					&& Strings.nullOrEqual(entry.scenario, amount.scenario))
				return amount;
		}
		return null;
	}

}
