package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import epd.io.EpdStore;
import epd.io.MappingConfig;
import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.Indicator;
import epd.model.IndicatorGroup;
import epd.model.IndicatorMapping;
import epd.model.IndicatorResult;

class ResultConverter {

	static List<IndicatorResult> readResults(Process process,
			MappingConfig config) {
		if (process == null || config == null)
			return Collections.emptyList();
		List<IndicatorResult> results = new ArrayList<>();
		results.addAll(readLciResults(process, config));
		results.addAll(readLciaResults(process, config));
		return results;
	}

	private static List<IndicatorResult> readLciResults(Process process,
			MappingConfig config) {
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
			MappingConfig config) {
		List<IndicatorResult> results = new ArrayList<>();
		for (LCIAResult element : process.lciaResults) {
			IndicatorResult result = readResult(element.method,
					element.other, config);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static IndicatorResult readResult(Ref ref,
			Other extension, MappingConfig config) {
		if (ref == null)
			return null;
		Indicator indicator = config.getIndicator(ref.uuid);
		if (indicator == null)
			return null;
		IndicatorResult result = new IndicatorResult();
		result.indicator = indicator;
		List<Amount> amounts = AmountConverter.readAmounts(extension);
		result.amounts.addAll(amounts);
		return result;
	}

	static void writeResults(EpdDataSet ds, MappingConfig config) {
		if (Util.hasNull(ds, ds.process, config))
			return;
		Document doc = Util.createDocument();
		for (IndicatorResult result : ds.results) {
			Indicator indicator = result.indicator;
			IndicatorMapping mapping = config.getIndicatorMapping(indicator);
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

	private static Other createLciResult(Process process,
			IndicatorMapping mapping) {
		Exchange exchange = new Exchange();
		exchange.id = process.exchanges.size();
		process.exchanges.add(exchange);
		exchange.flow = createRef(mapping, true);
		setExchangeAttributes(mapping, exchange);
		Other other = new Other();
		exchange.other = other;
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
		LangString.set(ref.name, mapping.indicatorLabel, EpdStore.lang);
		return ref;
	}

	private static void addUnitRef(Other other, IndicatorMapping mapping,
			Document doc) {
		if (other == null || mapping == null)
			return;
		Element root = doc.createElementNS(Converter.NAMESPACE,
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
