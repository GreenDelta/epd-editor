package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.ImpactResult;
import org.openlca.ilcd.processes.Process;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import app.App;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;
import epd.model.Indicator;
import epd.model.Indicator.Type;
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
			var result = readResult(exchange.getFlow(), exchange.getOther(), profile);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static List<IndicatorResult> readLciaResults(
			Process process, EpdProfile profile) {
		List<IndicatorResult> results = new ArrayList<>();
		for (var r : process.getImpactResults()) {
			var result = readResult(r.getMethod(), r.getOther(), profile);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static IndicatorResult readResult(
			Ref ref, Other extension, EpdProfile profile) {
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

	static void writeResults(EpdDataSet epd) {
		if (epd == null || epd.process == null)
			return;
		var doc = Dom.createDocument();
		for (var result : epd.results) {
			var indicator = result.indicator;
			if (indicator == null)
				continue;
			var other = indicator.type == Type.LCI
					? initFlow(epd.process, indicator)
					: initImpact(epd.process, indicator);
			AmountConverter.writeAmounts(result.amounts, other, doc);
			addUnitRef(other, indicator, doc);
		}
	}

	private static Other initFlow(Process p, Indicator indicator) {
		int nextId = 1;
		for (var e : p.getExchanges()) {
			if (e.getId() >= nextId) {
				nextId = e.getId() + 1;
			}
		}
		var e = new Exchange()
				.withId(nextId)
				.withFlow(refOf(indicator))
				.withExchangeFunction(ExchangeFunction.GENERAL_REMINDER_FLOW)
				.withDirection(indicator.isInput != null && indicator.isInput
						? ExchangeDirection.INPUT
						: ExchangeDirection.OUTPUT);
		p.withExchanges().add(e);
		return e.withOther();
	}

	private static Other initImpact(Process process, Indicator indicator) {
		var r = new ImpactResult()
				.withMethod(refOf(indicator));
		process.withImpactResults().add(r);
		return r.withOther();
	}

	private static Ref refOf(Indicator indicator) {
		if (indicator == null)
			return null;
		var ref = indicator.getRef(App.lang());
		var indexRef = App.index().find(ref);
		return indexRef != null ? indexRef : ref;
	}

	private static void addUnitRef(
			Other other, Indicator indicator, Document doc) {
		if (other == null || indicator == null)
			return;
		Element root = doc.createElementNS(Vocab.NS_EPD,
				"epd:referenceToUnitGroupDataSet");
		root.setAttribute("type", "unit group data set");
		root.setAttribute("refObjectId", indicator.unitGroupUUID);
		String uri = "../unitgroups/" + indicator.unitGroupUUID;
		root.setAttribute("uri", uri);
		Element description = doc.createElementNS(
				"http://lca.jrc.it/ILCD/Common", "common:shortDescription");
		description.setTextContent(indicator.unit);
		root.appendChild(description);
		other.withAny().add(root);
	}
}
