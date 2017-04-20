package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.LCIAResult;
import org.openlca.ilcd.processes.Process;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import app.App;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.Indicator;
import epd.model.IndicatorGroup;
import epd.model.IndicatorMapping;
import epd.model.IndicatorResult;

class ResultConverter {

	static List<IndicatorResult> readResults(Process process,
			List<IndicatorMapping> config) {
		if (process == null || config == null)
			return Collections.emptyList();
		List<IndicatorResult> results = new ArrayList<>();
		results.addAll(readLciResults(process, config));
		results.addAll(readLciaResults(process, config));
		return results;
	}

	private static List<IndicatorResult> readLciResults(Process process,
			List<IndicatorMapping> config) {
		List<IndicatorResult> results = new ArrayList<>();
		for (Exchange exchange : process.exchanges) {
			IndicatorResult result = readResult(exchange.flow,
					exchange.other, config);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static List<IndicatorResult> readLciaResults(Process process,
			List<IndicatorMapping> config) {
		List<IndicatorResult> results = new ArrayList<>();
		if (process.lciaResults == null)
			return results;
		for (LCIAResult element : process.lciaResults) {
			IndicatorResult result = readResult(element.method,
					element.other, config);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static IndicatorResult readResult(Ref ref,
			Other extension, List<IndicatorMapping> indicators) {
		if (ref == null)
			return null;
		Indicator indicator = get(indicators, ref.uuid);
		if (indicator == null)
			return null;
		IndicatorResult result = new IndicatorResult();
		result.indicator = indicator;
		List<Amount> amounts = AmountConverter.readAmounts(extension);
		result.amounts.addAll(amounts);
		return result;
	}

	private static Indicator get(List<IndicatorMapping> indicators,
			String uuid) {
		for (IndicatorMapping i : indicators) {
			if (Objects.equals(i.indicatorRefId, uuid))
				return i.indicator;
		}
		return null;
	}

	static void writeResults(EpdDataSet ds, List<IndicatorMapping> indicators) {
		if (Util.hasNull(ds, ds.process, indicators))
			return;
		Document doc = Util.createDocument();
		for (IndicatorResult result : ds.results) {
			Indicator indicator = result.indicator;
			IndicatorMapping mapping = get(indicators, indicator);
			if (mapping == null)
				continue;
			Other other = null;
			if (indicator.isInventoryIndicator())
				other = createLciResult(ds.process, mapping);
			else
				other = createLciaResult(ds.process, mapping);
			if (other != null) {
				AmountConverter.writeAmounts(result.amounts, other, doc);
				addUnitRef(other, mapping, doc);
			}
		}
	}

	private static IndicatorMapping get(List<IndicatorMapping> indicators,
			Indicator indicator) {
		for (IndicatorMapping i : indicators) {
			if (i.indicator == indicator)
				return i;
		}
		return null;
	}

	private static Other createLciResult(Process p, IndicatorMapping mapping) {
		int nextId = 1;
		for (Exchange e : p.exchanges) {
			if (e.id >= nextId)
				nextId = e.id + 1;
		}
		Exchange e = new Exchange();
		e.id = nextId;
		p.exchanges.add(e);
		e.flow = createRef(mapping, true);
		setExchangeAttributes(mapping, e);
		Other other = new Other();
		e.other = other;
		return other;
	}

	private static void setExchangeAttributes(IndicatorMapping mapping,
			Exchange exchange) {
		exchange.exchangeFunction = ExchangeFunction.GENERAL_REMINDER_FLOW;
		Indicator indicator = mapping.indicator;
		if (indicator == null)
			return;
		if (indicator.getGroup() == IndicatorGroup.RESOURCE_USE)
			exchange.direction = ExchangeDirection.INPUT;
		else
			exchange.direction = ExchangeDirection.OUTPUT;
	}

	private static Other createLciaResult(Process process,
			IndicatorMapping mapping) {
		LCIAResult r = new LCIAResult();
		process.add(r);
		r.method = createRef(mapping, false);
		Other other = new Other();
		r.other = other;
		return other;
	}

	private static Ref createRef(IndicatorMapping mapping,
			boolean forFlow) {
		if (mapping == null)
			return null;
		Ref ref = new Ref();
		ref.uuid = mapping.indicatorRefId;
		String path = forFlow ? "flows" : "lciamethods";
		ref.uri = "../" + path + "/" + mapping.indicatorRefId;
		ref.type = forFlow ? DataSetType.FLOW
				: DataSetType.LCIA_METHOD;
		LangString.set(ref.name, mapping.indicatorLabel, App.lang());
		return ref;
	}

	private static void addUnitRef(Other other, IndicatorMapping mapping,
			Document doc) {
		if (other == null || mapping == null)
			return;
		Element root = doc.createElementNS(ProcessExtensions.NAMESPACE,
				"epd:referenceToUnitGroupDataSet");
		root.setAttribute("type", "unit group data set");
		root.setAttribute("refObjectId", mapping.unitRefId);
		String uri = "../unitgroups/" + mapping.unitRefId;
		root.setAttribute("uri", uri);
		Element description = doc.createElementNS(
				"http://lca.jrc.it/ILCD/Common", "common:shortDescription");
		description.setTextContent(mapping.unitLabel);
		root.appendChild(description);
		other.any.add(root);
	}
}
