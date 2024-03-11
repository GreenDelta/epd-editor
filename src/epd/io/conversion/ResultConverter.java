package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Extension;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;

import epd.model.Amount;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.IndicatorResult;

class ResultConverter {

	static List<IndicatorResult> readResults(
			Process process, EpdProfile profile) {
		if (process == null || profile == null)
			return Collections.emptyList();
		List<IndicatorResult> results = new ArrayList<>();
		results.addAll(readLciResults(process, profile));
		results.addAll(readLciaResults(process, profile));
		return results;
	}

	private static List<IndicatorResult> readLciResults(
			Process process, EpdProfile profile) {
		List<IndicatorResult> results = new ArrayList<>();
		for (Exchange exchange : process.getExchanges()) {
			var result = readResult(exchange.getFlow(), exchange.getEpdExtension(), profile);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static List<IndicatorResult> readLciaResults(
			Process process, EpdProfile profile) {
		List<IndicatorResult> results = new ArrayList<>();
		for (var r : process.getImpactResults()) {
			var result = readResult(r.getMethod(), r.getEpdExtension(), profile);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static IndicatorResult readResult(
			Ref ref, Extension extension, EpdProfile profile) {
		if (ref == null)
			return null;
		Indicator indicator = profile.indicator(ref.getUUID());
		if (indicator == null)
			return null;
		IndicatorResult result = new IndicatorResult();
		result.indicator = indicator;
		List<Amount> amounts = AmountConverter.readAmounts(
				extension, profile);
		result.amounts.addAll(amounts);
		return result;
	}
}
