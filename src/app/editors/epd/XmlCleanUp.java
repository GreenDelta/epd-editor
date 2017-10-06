package app.editors.epd;

import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.util.Processes;

import epd.model.EpdDataSet;

/**
 * Delete empty and invalid elements from the data set. Note that these elements
 * are sometimes created for the data binding in the editor.
 */
class XmlCleanUp {

	static void on(EpdDataSet ds) {
		if (ds == null || ds.process == null)
			return;
		new XmlCleanUp(ds).doIt();
	}

	private final EpdDataSet ds;

	private XmlCleanUp(EpdDataSet ds) {
		this.ds = ds;
	}

	private void doIt() {
		removeDataGenerator();
		checkTime();
	}

	private void removeDataGenerator() {
		Method m = Processes.getMethod(ds.process);
		ProcessType type = Processes.getMethod(ds.process).processType;
		if (m != null && type == ProcessType.EPD) {
			AdminInfo info = Processes.getAdminInfo(ds.process);
			// the dataGenerator element is not allowed in EPD data sets
			if (info != null)
				info.dataGenerator = null;
		}
	}

	private void checkTime() {
		Time time = Processes.getTime(ds.process);
		if (time == null)
			return;
		if (!time.description.isEmpty())
			return;
		if (time.referenceYear != null)
			return;
		if (time.validUntil != null)
			return;
		ds.process.processInfo.time = null;
	}

}
