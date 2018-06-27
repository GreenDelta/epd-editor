package app.editors.epd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import app.store.EpdProfiles;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.IndicatorResult;
import epd.model.ModuleEntry;
import epd.util.Strings;

/**
 * Initializes indicator results for added modules and removes results for
 * module entries that no longer exist in an EPD data set.
 */
class ModuleResultSync implements Runnable {

	private final EpdDataSet dataSet;

	public ModuleResultSync(EpdDataSet dataSet) {
		this.dataSet = dataSet;
	}

	public void run() {
		HashMap<String, ModuleEntry> index = new HashMap<>();
		for (ModuleEntry entry : dataSet.moduleEntries) {
			index.put(key(entry), entry);
		}
		removeOld(index);
		syncNew(index);
	}

	private HashMap<String, Boolean> removeOld(
			HashMap<String, ModuleEntry> index) {
		HashMap<String, Boolean> found = new HashMap<>();
		for (IndicatorResult result : dataSet.results) {
			List<Amount> removals = new ArrayList<>();
			for (Amount amount : result.amounts) {
				String key = key(amount);
				if (!index.containsKey(key))
					removals.add(amount);
				else
					found.put(key, true);
			}
			result.amounts.removeAll(removals);
		}
		return found;
	}

	private void syncNew(HashMap<String, ModuleEntry> index) {
		EpdProfile profile = EpdProfiles.get(dataSet.profile);
		for (String key : index.keySet()) {
			ModuleEntry entry = index.get(key);
			if (entry == null)
				continue;
			syncResults(entry, profile);
		}
	}

	private void syncResults(ModuleEntry entry, EpdProfile profile) {
		for (Indicator indicator : profile.indicators) {
			IndicatorResult result = dataSet.getResult(indicator);
			if (result == null) {
				result = new IndicatorResult();
				result.indicator = indicator;
				dataSet.results.add(result);
			}
			Amount amount = findAmount(result, entry);
			if (amount != null)
				continue;
			amount = new Amount();
			result.amounts.add(amount);
			amount.module = entry.module;
			amount.scenario = entry.scenario;
		}
	}

	private Amount findAmount(IndicatorResult result, ModuleEntry entry) {
		for (Amount amount : result.amounts) {
			if (Objects.equals(entry.module, amount.module)
					&& Strings.nullOrEqual(entry.scenario, amount.scenario))
				return amount;
		}
		return null;
	}

	private String key(ModuleEntry entry) {
		return Objects.toString(entry.module)
				+ Objects.toString(entry.scenario);
	}

	private String key(Amount amount) {
		return Objects.toString(amount.module)
				+ Objects.toString(amount.scenario);
	}

}
