package epd.io.conversion;

import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import epd.io.MappingConfig;
import epd.model.Amount;
import epd.model.DeclaredProduct;
import epd.model.EpdDataSet;
import epd.model.IndicatorResult;
import epd.model.ModuleEntry;
import epd.model.Ref;
import epd.model.Scenario;
import epd.model.SubType;

/**
 * Converts an ILCD process data set to an EPD data set.
 */
class ProcessConverter {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Process process;
	private final MappingConfig config;
	private String[] langs;

	public ProcessConverter(Process process, MappingConfig config) {
		this.process = process;
		this.config = config;
	}

	public EpdDataSet convert(String[] langs) {
		if (process == null)
			return null;
		this.langs = langs;
		EpdDataSet dataSet = new EpdDataSet();
		dataSet.processInfo = process.processInfo;
		dataSet.modelling = process.modelling;
		dataSet.adminInfo = process.adminInfo;
		mapDeclaredProduct(dataSet);
		readExtensions(dataSet);
		mapResults(dataSet);
		return dataSet;
	}

	private void mapDeclaredProduct(EpdDataSet dataSet) {
		DeclaredProduct product = new DeclaredProduct();
		dataSet.declaredProduct = product;
		Exchange exchange = getProductExchange();
		if (exchange == null) {
			log.warn("could not find a reference flow in data set {}", dataSet);
			return;
		}
		product.flow = Ref.of(exchange.flow, langs);
		if (exchange.resultingAmount != null)
			product.amount = exchange.resultingAmount;
	}

	private Exchange getProductExchange() {
		ProcessInfo processInfo = process.processInfo;
		if (processInfo == null)
			return null;
		QuantitativeReference qRef = processInfo.quantitativeReference;
		if (qRef == null || qRef.referenceFlows.isEmpty())
			return null;
		Integer id = qRef.referenceFlows.get(0);
		if (id == null)
			return null;
		for (Exchange exchange : process.exchanges) {
			if (id == exchange.id)
				return exchange;
		}
		return null;
	}

	private void readExtensions(EpdDataSet dataSet) {
		ProcessInfo processInfo = process.processInfo;
		if (processInfo == null)
			return;
		org.openlca.ilcd.processes.DataSetInfo dataSetInfo = processInfo.dataSetInfo;
		if (dataSetInfo == null || dataSetInfo.other == null)
			return;
		Other other = dataSetInfo.other;
		List<Scenario> scenarios = ScenarioConverter.readScenarios(other);
		dataSet.scenarios.addAll(scenarios);
		List<ModuleEntry> modules = ModuleConverter.readModules(other);
		dataSet.moduleEntries.addAll(modules);
		dataSet.safetyMargins = SafetyMarginsConverter.read(other);
		readSubType(dataSet);
	}

	private void readSubType(EpdDataSet dataSet) {
		if (process.modelling == null)
			return;
		Method method = process.modelling.method;
		if (method == null || method.other == null)
			return;
		Element e = Util.getElement(method.other, "subType");
		if (e != null) {
			SubType type = SubType.fromLabel(e.getTextContent());
			dataSet.subType = type;
		}
	}

	private void mapResults(EpdDataSet dataSet) {
		List<IndicatorResult> results = ResultConverter.readResults(process,
				config);
		dataSet.results.addAll(results);
		// data sets may not have the module-entry extension, thus we have to
		// find the module entries for such data sets from the results
		for (IndicatorResult result : results) {
			for (Amount amount : result.amounts) {
				ModuleEntry entry = findModuleEntry(dataSet, amount);
				if (entry != null)
					continue;
				entry = new ModuleEntry();
				entry.module = amount.module;
				entry.scenario = amount.scenario;
				dataSet.moduleEntries.add(entry);
			}
		}
	}

	private ModuleEntry findModuleEntry(EpdDataSet dataSet, Amount amount) {
		for (ModuleEntry entry : dataSet.moduleEntries) {
			if (Objects.equals(entry.module, amount.module)
					&& Objects
							.equals(entry.scenario, amount.scenario))
				return entry;
		}
		return null;
	}
}
