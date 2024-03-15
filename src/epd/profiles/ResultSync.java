package epd.profiles;

import epd.util.Strings;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.EpdIndicatorResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Get the indicator results of an EPD but with the corresponding
 * indicator and unit group definitions from a profile when available.
 * In the editor, we always want to display and use the definitions
 * from the profiles if possible.
 */
class ResultSync {

	static List<EpdIndicatorResult> of(Process epd, EpdProfile profile) {
		var results = EpdIndicatorResult.allOf(epd);
		if (results.isEmpty() || profile == null)
			return results;

		var indicators = new HashMap<String, Indicator>();
		for (var i : profile.getIndicators()) {
			indicators.put(i.getUUID(), i);
		}

		var mapped = new ArrayList<EpdIndicatorResult>();
		for (var r : results) {
			var ref = r.indicator();
			if (ref == null || Strings.nullOrEmpty(ref.getUUID())) {
				mapped.add(r);
				continue;
			}
			var i = indicators.get(ref.getUUID());
			if (i == null) {
				mapped.add(r);
				continue;
			}

			// we add the values of the underlying EPD extensions
			// that can be then directly mutated in the editor
			mapped.add(new EpdIndicatorResult(
				i.getRef(),
				i.getUnit(),
				r.values(),
				i.isInputIndicator()));
		}

		return mapped;
	}


}
