package epd.io.conversion;

import org.openlca.ilcd.Vocab;
import org.openlca.ilcd.processes.Modelling;
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
		writeExtensions();
		Cleanup.on(epd);
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
		if (epd.contentDeclaration != null) {
			epd.contentDeclaration.write(infoOther, doc);
		}
		if (Cleanup.isEmpty(infoOther)) {
			info.withEpdExtension(null);
		}
	}
}
