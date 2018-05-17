package epd.io.conversion;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.util.Processes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import epd.model.EpdDataSet;
import epd.model.EpdProfile;

/**
 * Converts an EPD to an ILCD process data set
 */
class EpdConverter {

	private final EpdDataSet dataSet;
	private final EpdProfile profile;

	public EpdConverter(EpdDataSet dataSet, EpdProfile profile) {
		this.dataSet = dataSet;
		this.profile = profile;
	}

	public void convert() {
		if (dataSet == null)
			return;
		if (dataSet.process == null)
			dataSet.process = new Process();
		clearResults(dataSet.process);
		ResultConverter.writeResults(dataSet, profile);
		writeExtensions();
	}

	/** Remove all result exchanges. */
	private void clearResults(Process p) {
		if (p == null)
			return;
		QuantitativeReference qref = Processes.getQuantitativeReference(p);
		List<Integer> refFlows = qref == null ? Collections.emptyList()
				: qref.referenceFlows;
		p.exchanges.removeIf(e -> !refFlows.contains(e.id));
		p.lciaResults = null;
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
		if (Util.isEmpty(other))
			info.other = null;
	}

	private void writeSubType() {
		if (dataSet.subType == null) {
			Method m = Processes.getMethod(dataSet.process);
			if (m == null)
				return;
			m.other = null;
			return;
		}
		Method method = Processes.method(dataSet.process);
		method.other = new Other();
		Element e = Util.createElement(method.other, "subType");
		e.setTextContent(dataSet.subType.getLabel());
		method.other.any.add(e);
	}
}