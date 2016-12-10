package epd.io.conversion;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
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

	public EpdConverter(EpdDataSet dataSet, MappingConfig config) {
		this.dataSet = dataSet;
		this.config = config;
	}

	public void convert() {
		if (dataSet == null)
			return;
		if (dataSet.process == null)
			dataSet.process = new Process();
		ResultConverter.writeResults(dataSet, config);
		writeExtensions();
	}

	private void writeExtensions() {
		DataSetInfo info = Processes.dataSetInfo(dataSet.process);
		Other other = info.other;
		if (other == null) {
			other = new Other();
			info.other = other;
		}
		Document doc = Util.createDocument();
		ModuleConverter.writeModules(dataSet, other, doc);
		ScenarioConverter.writeScenarios(dataSet, other, doc);
		SafetyMarginsConverter.write(dataSet, other, doc);
		writeSubType();
	}

	private void writeSubType() {
		Method method = Processes.method(dataSet.process);
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