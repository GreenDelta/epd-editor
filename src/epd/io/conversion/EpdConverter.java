package epd.io.conversion;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import epd.io.MappingConfig;
import epd.model.EpdDataSet;

/**
 * Converts an EPD to an ILCD process data set
 */
class EpdConverter {

	private final EpdDataSet dataSet;
	private final MappingConfig config;
	private Process process;

	public EpdConverter(EpdDataSet dataSet, MappingConfig config) {
		this.dataSet = dataSet;
		this.config = config;
	}

	public Process convert() {
		if (dataSet == null)
			return null;
		process = new Process();
		process.version = "1.1";
		dataSet.process = process;
		ResultConverter.writeResults(dataSet, process, config);
		writeExtensions();
		return process;
	}

	private void writeExtensions() {
		ProcessInfo processInfo = getProcessInfo();
		DataSetInfo dataSetInfo = processInfo.dataSetInfo;
		if (dataSetInfo == null) {
			dataSetInfo = new DataSetInfo();
			processInfo.dataSetInfo = dataSetInfo;
		}
		Other other = dataSetInfo.other;
		if (other == null) {
			other = new Other();
			dataSetInfo.other = other;
		}
		Document doc = Util.createDocument();
		ModuleConverter.writeModules(dataSet, other, doc);
		ScenarioConverter.writeScenarios(dataSet, other, doc);
		SafetyMarginsConverter.write(dataSet, other, doc);
		writeSubType();
	}

	private ProcessInfo getProcessInfo() {
		ProcessInfo processInfo = process.processInfo;
		if (processInfo == null) {
			processInfo = new ProcessInfo();
			process.processInfo = processInfo;
		}
		return processInfo;
	}

	private void writeSubType() {
		if (process.modelling == null)
			process.modelling = new Modelling();
		Method method = process.modelling.method;
		if (method == null) {
			method = new Method();
			process.modelling.method = method;
		}
		if (method.other == null)
			method.other = new Other();
		Util.clear(method.other, "subType");
		if (dataSet.subType == null)
			return;
		Element e = Util.createElement(method.other, "subType");
		e.setTextContent(dataSet.subType.getLabel());
		method.other.any.add(e);
	}
}