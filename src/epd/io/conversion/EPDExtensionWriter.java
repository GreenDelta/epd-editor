package epd.io.conversion;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

import epd.model.EpdDataSet;

class EPDExtensionWriter {

	private final EpdDataSet epd;

	private EPDExtensionWriter(EpdDataSet dataSet) {
		this.epd = dataSet;
	}

	/**
	 * Writes the EPD extensions into the wrapped ILCD process of the given EPD
	 * data set.
	 */
	static void write(EpdDataSet epd) {
		new EPDExtensionWriter(epd).write();
	}

	private void write() {
		if (epd == null)
			return;
		var process = epd.process;
		clearResults(process);
		ResultConverter.writeResults(epd);
		writeExtensions();
		// set the format version
		process.withOtherAttributes().put(
			new QName(Vocab.NS_EPDv2, "epd-version", "epd2"), "1.2");
		process.withSchemaVersion("1.1");
		Cleanup.on(epd);
	}

	/**
	 * Remove all result exchanges.
	 */
	private void clearResults(Process p) {
		if (p == null)
			return;
		p.withImpactResults(null);
		if (p.getExchanges().isEmpty())
			return;
		var qRef = Processes.getQuantitativeReference(p);
		List<Integer> refFlows = qRef == null
			? Collections.emptyList()
			: qRef.getReferenceFlows();
		p.getExchanges().removeIf(e -> !refFlows.contains(e.getId()));
	}

	private void writeExtensions() {
		var doc = Dom.createDocument();

		// write the Q-Meta data
		var qMeta = epd.qMetaData;
		if (qMeta == null) {
			Modelling mod = Processes.getModelling(epd.process);
			if (mod != null) {
				// remove possible old data
				mod.withOther(null);
			}
		} else {
			var other = epd.process.withModelling().withOther();
			epd.qMetaData.write(other, doc);
		}

		// write info extensions
		var info = epd.process.withProcessInfo()
			.withDataSetInfo();
		var infoOther = info.getEpdExtension();
		ModuleConverter.writeModules(epd, infoOther, doc);
		ScenarioConverter.writeScenarios(epd, infoOther, doc);
		SafetyMarginsConverter.write(epd, infoOther, doc);
		if (epd.contentDeclaration != null) {
			epd.contentDeclaration.write(infoOther, doc);
		}
		if (Dom.isEmpty(infoOther)) {
			info.withEpdExtension(null);
		}

		writeProfile();
		writeSubType();
		writePublicationDate();
		PublisherRef.write(epd);
		OriginalEPDRef.write(epd);
	}

	private void writeSubType() {
		if (epd.subType == null) {
			var m = Processes.getInventoryMethod(epd.process);
			if (m == null)
				return;
			m.withEpdExtension(null);
			return;
		}
		var other = epd.process.withModelling()
			.withInventoryMethod()
			.withEpdExtension();
		var elem = Dom.createElement(Vocab.NS_EPD, "subType");
		if (elem != null) {
			elem.setTextContent(epd.subType.getLabel());
			other.withAny().add(elem);
		}
	}

	private void writeProfile() {
		if (epd.profile != null) {
			epd.process.withOtherAttributes()
				.put(Vocab.PROFILE_ATTR, epd.profile);
		} else {
			var atts = epd.process.getOtherAttributes();
			if (!atts.isEmpty()) {
				atts.remove(Vocab.PROFILE_ATTR);
			}
		}
	}

	private void writePublicationDate() {
		var t = Processes.getTime(epd.process);
		var pubDate = epd.publicationDate;
		if (pubDate == null && t == null)
			return;
		var time = t == null
			? epd.process.withProcessInfo().withTime()
			: t;
		if (pubDate == null && time.getEpdExtension() == null)
			return;
		var tag = "publicationDateOfEPD";

		// delete it if publication date is null
		if (pubDate == null) {
			Dom.clear(time.getEpdExtension(), tag);
			if (Dom.isEmpty(time.getEpdExtension())) {
				time.withEpdExtension(null);
			}
			return;
		}

		// create or update the element
		var elem = Dom.getElement(time.getEpdExtension(), tag);
		if (elem != null) {
			elem.setTextContent(pubDate.toString());
			return;
		}
		var newElem = Dom.createElement(Vocab.NS_EPDv2, tag);
		if (newElem == null)
			return;
		newElem.setTextContent(pubDate.toString());
		time.withEpdExtension().withAny().add(newElem);
	}
}
