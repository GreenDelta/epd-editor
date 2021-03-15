package epd.io.conversion;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.util.Processes;

import epd.model.EpdDataSet;

/**
 * Converts an EPD to an ILCD process data set
 */
class EpdConverter {

	private final EpdDataSet dataSet;

	public EpdConverter(EpdDataSet dataSet) {
		this.dataSet = dataSet;
	}

	public void convert() {
		if (dataSet == null)
			return;
		if (dataSet.process == null) {
			dataSet.process = new Process();
		}
		clearResults(dataSet.process);
		ResultConverter.writeResults(dataSet);
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
		var doc = Dom.createDocument();

		// write the Q-Meta data
		var qmeta = dataSet.qMetaData;
		if (qmeta == null) {
			Modelling mod = Processes.getModelling(dataSet.process);
			if (mod != null) {
				// remove possible old data
				mod.other = null;
			}
		} else {
			Modelling mod = Processes.modelling(dataSet.process);
			if (mod.other == null) {
				mod.other = new Other();
			}
			dataSet.qMetaData.write(mod.other, doc);
		}

		// write the other extension elements
		DataSetInfo info = Processes.dataSetInfo(dataSet.process);
		Other other = info.other;
		if (other == null) {
			other = new Other();
			info.other = other;
		}
		ModuleConverter.writeModules(dataSet, other, doc);
		ScenarioConverter.writeScenarios(dataSet, other, doc);
		SafetyMarginsConverter.write(dataSet, other, doc);
		if (dataSet.contentDeclaration != null) {
			dataSet.contentDeclaration.write(other, doc);
		}
		writeProfile();
		writeSubType();
		if (Dom.isEmpty(other)) {
			info.other = null;
		}
	}

	private void writeSubType() {
		if (dataSet.subType == null) {
			Method m = Processes.getMethod(dataSet.process);
			if (m == null)
				return;
			m.other = null;
			return;
		}
		var method = Processes.method(dataSet.process);
		method.other = new Other();
		var elem = Dom.createElement("subType");
		if (elem != null) {
			elem.setTextContent(dataSet.subType.getLabel());
			method.other.any.add(elem);
		}
	}

	private void writeProfile() {
		Map<QName, String> atts = dataSet.process.otherAttributes;
		if (dataSet.profile != null) {
			atts.put(Vocab.PROFILE_ATTR, dataSet.profile);
		} else {
			atts.remove(Vocab.PROFILE_ATTR);
		}
	}
}
