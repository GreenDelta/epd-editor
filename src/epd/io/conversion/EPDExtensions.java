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
import org.openlca.ilcd.util.Processes;

import epd.model.EpdDataSet;

class EPDExtensions {

	private final EpdDataSet epd;

	private EPDExtensions(EpdDataSet dataSet) {
		this.epd = dataSet;
	}

	/**
	 * Writes the EPD extensions into the wrapped ILCD process of the given EPD
	 * data set.
	 */
	static void write(EpdDataSet epd) {
		new EPDExtensions(epd).write();
	}

	private void write() {
		if (epd == null)
			return;
		var process = epd.process;
		clearResults(process);
		ResultConverter.writeResults(epd);
		writeExtensions();

		// set the format version
		process.otherAttributes.put(
				new QName(Vocab.NS_EPDv2, "epd-version", "epd2"), "1.2");
		process.version = "1.1";
	}

	/**
	 * Remove all result exchanges.
	 */
	private void clearResults(Process p) {
		if (p == null)
			return;
		var qRef = Processes.getQuantitativeReference(p);
		List<Integer> refFlows = qRef == null
				? Collections.emptyList()
				: qRef.referenceFlows;
		p.exchanges.removeIf(e -> !refFlows.contains(e.id));
		p.lciaResults = null;
	}

	private void writeExtensions() {
		var doc = Dom.createDocument();

		// write the Q-Meta data
		var qMeta = epd.qMetaData;
		if (qMeta == null) {
			Modelling mod = Processes.getModelling(epd.process);
			if (mod != null) {
				// remove possible old data
				mod.other = null;
			}
		} else {
			Modelling mod = Processes.modelling(epd.process);
			if (mod.other == null) {
				mod.other = new Other();
			}
			epd.qMetaData.write(mod.other, doc);
		}

		// write the other extension elements
		DataSetInfo info = Processes.dataSetInfo(epd.process);
		Other other = info.other;
		if (other == null) {
			other = new Other();
			info.other = other;
		}
		ModuleConverter.writeModules(epd, other, doc);
		ScenarioConverter.writeScenarios(epd, other, doc);
		SafetyMarginsConverter.write(epd, other, doc);
		if (epd.contentDeclaration != null) {
			epd.contentDeclaration.write(other, doc);
		}
		writeProfile();
		writeSubType();
		if (Dom.isEmpty(other)) {
			info.other = null;
		}
	}

	private void writeSubType() {
		if (epd.subType == null) {
			Method m = Processes.getMethod(epd.process);
			if (m == null)
				return;
			m.other = null;
			return;
		}
		var method = Processes.method(epd.process);
		method.other = new Other();
		var elem = Dom.createElement("subType");
		if (elem != null) {
			elem.setTextContent(epd.subType.getLabel());
			method.other.any.add(elem);
		}
	}

	private void writeProfile() {
		Map<QName, String> atts = epd.process.otherAttributes;
		if (epd.profile != null) {
			atts.put(Vocab.PROFILE_ATTR, epd.profile);
		} else {
			atts.remove(Vocab.PROFILE_ATTR);
		}
	}
}
