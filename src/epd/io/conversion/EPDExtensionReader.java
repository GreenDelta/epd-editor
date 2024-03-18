package epd.io.conversion;

import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

import epd.model.EpdDataSet;
import epd.model.qmeta.QMetaData;

/**
 * Converts an ILCD process data set to an EPD data set.
 */
class EPDExtensionReader {

	private final Process process;

	private EPDExtensionReader(Process process) {
		this.process = process;
	}

	static EpdDataSet read(Process process) {
		return new EPDExtensionReader(process).read();
	}

	private EpdDataSet read() {
		var epd = new EpdDataSet(process);
		readExtensions(epd);
		return epd;
	}

	private void readExtensions(EpdDataSet epd) {
		epd.qMetaData = QMetaData.read(process);

		// read the extensions that are stored under `dataSetInformation`
		var info = Processes.getDataSetInfo(process);
		if (info == null || info.getEpdExtension() == null)
			return;
		var other = info.getEpdExtension();
	}
}
