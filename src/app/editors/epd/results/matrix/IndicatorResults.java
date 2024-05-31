package app.editors.epd.results.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileIndicator;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.ImpactResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdResultExtension;

import epd.util.Strings;

class IndicatorResults {

	static List<IndicatorResult> of(
			Process epd, EpdProfile profile, Mod[] mods
	) {

		var profileIdx = new HashMap<String, EpdProfileIndicator>();
		for (var i : profile.getIndicators()) {
			profileIdx.put(i.getUUID(), i);
		}

		var modIdx = new HashMap<String, Integer>();
		for (int i = 0; i < mods.length; i++) {
			modIdx.put(mods[i].key(), i);
		}

		var results = new ArrayList<IndicatorResult>();
		for (var e : epd.getExchanges()) {
			if (!InventoryElem.isElem(e))
				continue;
			var id = e.getFlow().getUUID();
			var pi = profileIdx.get(id);
			if (pi != null) {
				e.withFlow(pi.getRef());
				e.withEpdExtension().withUnitGroup(pi.getUnit());
			}
			results.add(new InventoryElem(
					e, pi, new Double[mods.length]));
		}

		for (var r : epd.getImpactResults()) {
			if (!ImpactElem.isElem(r))
				continue;
			var id = r.getMethod().getUUID();
			var pi = profileIdx.get(id);
			if (pi != null) {
				r.withMethod(pi.getRef());
				r.withEpdExtension().withUnitGroup(pi.getUnit());
			}
			results.add(new ImpactElem(
					r, pi, new Double[mods.length]));
		}


		for (var r : results) {
			putModValues(r, modIdx);
		}
		sort(results);

		return results;
	}

	private static void sort(ArrayList<IndicatorResult> results) {
		results.sort((r1, r2) -> {
			// impact indicators first
			var t1 = r1.indicator().getType();
			var t2 = r2.indicator().getType();
			if (t1 != t2)
				return t1 == DataSetType.IMPACT_METHOD ? -1 : 1;

			// then try by code
			var c1 = r1.getIndicatorCode();
			var c2 = r2.getIndicatorCode();
			if (Strings.notEmpty(c1) && Strings.notEmpty(c2))
				return Strings.compare(c1, c2);

			// finally compare by name
			return Strings.compare(
					r1.getIndicatorName(), r2.getIndicatorName());
		});
	}

	private static void putModValues(
			IndicatorResult r, HashMap<String, Integer> modIdx
	) {
		for (var v : r.getValues()) {
			var pos = modIdx.get(Mod.key(v));
			if (pos != null) {
				r.modValues()[pos] = v.getAmount();
			}
		}
	}

	private record InventoryElem(
			Exchange exchange,
			EpdProfileIndicator profileIndicator,
			Double[] modValues
	) implements IndicatorResult {

		static boolean isElem(Exchange e) {
			return e != null
					&& e.getFlow() != null
					&& e.getFlow().getUUID() != null
					&& e.getExchangeFunction() == ExchangeFunction.GENERAL_REMINDER_FLOW;
		}

		@Override
		public Ref indicator() {
			return exchange.getFlow();
		}

		@Override
		public EpdResultExtension ext() {
			return exchange.withEpdExtension();
		}
	}

	private record ImpactElem(
			ImpactResult impact,
			EpdProfileIndicator profileIndicator,
			Double[] modValues
	) implements IndicatorResult {

		static boolean isElem(ImpactResult r) {
			return r != null
					&& r.getMethod() != null
					&& r.getMethod().getUUID() != null;
		}

		@Override
		public Ref indicator() {
			return impact.getMethod();
		}

		@Override
		public EpdResultExtension ext() {
			return impact.withEpdExtension();
		}
	}
}
