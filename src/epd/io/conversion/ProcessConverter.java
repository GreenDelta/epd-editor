package epd.io.conversion;

import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.w3c.dom.Element;

import epd.model.Amount;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;
import epd.model.IndicatorResult;
import epd.model.ModuleEntry;
import epd.model.Scenario;
import epd.model.SubType;
import epd.model.content.ContentDeclaration;

/**
 * Converts an ILCD process data set to an EPD data set.
 */
class ProcessConverter {

	private final Process process;
	private final EpdProfile profile;

	public ProcessConverter(Process process, EpdProfile profile) {
		this.process = process;
		this.profile = profile;
	}

	public EpdDataSet convert() {
		if (process == null)
			return null;
		EpdDataSet dataSet = new EpdDataSet();
		dataSet.process = process;
		readExtensions(dataSet);
		mapResults(dataSet);
		return dataSet;
	}

	private void readExtensions(EpdDataSet dataSet) {
		dataSet.profile = process.otherAttributes.get(Vocab.PROFILE_ATTR);
		readSubType(dataSet);
		ProcessInfo processInfo = process.processInfo;
		if (processInfo == null)
			return;
		org.openlca.ilcd.processes.DataSetInfo dataSetInfo = processInfo.dataSetInfo;
		if (dataSetInfo == null || dataSetInfo.other == null)
			return;
		Other other = dataSetInfo.other;
		List<Scenario> scenarios = ScenarioConverter.readScenarios(other);
		dataSet.scenarios.addAll(scenarios);
		List<ModuleEntry> modules = ModuleConverter.readModules(other, profile);
		dataSet.moduleEntries.addAll(modules);
		dataSet.safetyMargins = SafetyMarginsConverter.read(other);
		dataSet.contentDeclaration = ContentDeclaration.fromXml(other);
	}

	private void readSubType(EpdDataSet dataSet) {
		if (process.modelling == null)
			return;
		Method method = process.modelling.method;
		if (method == null || method.other == null)
			return;
		Element e = Dom.getElement(method.other, "subType");
		if (e != null) {
			SubType type = SubType.fromLabel(e.getTextContent());
			dataSet.subType = type;
		}
	}

	private void mapResults(EpdDataSet dataSet) {
		List<IndicatorResult> results = ResultConverter.readResults(
				process, profile);
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
