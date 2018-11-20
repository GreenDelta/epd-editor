package app.store;

import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Processes;

import com.google.common.base.Strings;

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
		checkLocation();
		checkTechnology();
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

	private void checkLocation() {
		Geography geo = Processes.getGeography(ds.process);
		if (geo == null)
			return;
		Location loc = geo.location;
		if (loc == null) {
			ds.process.processInfo.geography = null;
			return;
		}
		if (!Strings.isNullOrEmpty(loc.code))
			return;
		if (!loc.description.isEmpty())
			return;
		ds.process.processInfo.geography = null;
	}

	private void checkTechnology() {
		Technology tech = Processes.getTechnology(ds.process);
		if (tech == null)
			return;
		if (!tech.applicability.isEmpty())
			return;
		if (!tech.description.isEmpty())
			return;
		if (!tech.includedProcesses.isEmpty())
			return;
		if (!tech.pictures.isEmpty())
			return;
		if (tech.pictogram != null)
			return;
		ds.process.processInfo.technology = null;
	}
}
