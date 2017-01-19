package epd.io.conversion;

import java.util.List;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import epd.model.EpdDataSet;
import epd.model.IndicatorMapping;

/**
 * Converts an EPD to an ILCD process data set
 */
class EpdConverter {

	private final EpdDataSet dataSet;
	private final List<IndicatorMapping> indicators;

	public EpdConverter(EpdDataSet dataSet, List<IndicatorMapping> indicators) {
		this.dataSet = dataSet;
		this.indicators = indicators;
	}

	public void convert() {
		if (dataSet == null)
			return;
		if (dataSet.process == null)
			dataSet.process = new Process();
		ResultConverter.writeResults(dataSet, indicators);
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