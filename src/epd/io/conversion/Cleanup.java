package epd.io.conversion;

import com.google.common.base.Strings;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.epd.matml.MaterialDoc;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Time;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.processes.epd.EpdInfoExtension;
import org.openlca.ilcd.processes.epd.EpdSafetyMargins;
import org.openlca.ilcd.processes.epd.EpdTimeExtension;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Processes;


/**
 * Remove empty elements so that the data set validation is happy.
 */
public class Cleanup {

	public static void on(Flow flow) {
		if (flow == null)
			return;
		var info = Flows.getDataSetInfo(flow);
		if (info != null && info.getEpdExtension() != null) {
			var ext = info.getEpdExtension();
			if (isEmpty(ext.getMaterialDoc())
				&& ext.getGenericFlow() == null
				&& ext.getAny().isEmpty()) {
				info.withEpdExtension(null);
			}
		}
	}

	private static boolean isEmpty(MaterialDoc doc) {
		if (doc == null)
			return true;
		return doc.getMaterials().isEmpty()
			&& doc.getProperties().isEmpty();
	}

	public static void on(Process epd) {
		if (epd == null)
			return;

		var info = Processes.getProcessInfo(epd);
		if (info != null && isEmpty(info.getTime())) {
			info.withTime(null);
		}
		if (info != null && isEmpty(info.getGeography())) {
			info.withGeography(null);
		}
		if (info != null && isEmpty(info.getTechnology())) {
			info.withTechnology(null);
		}

		var adminInfo = Processes.getAdminInfo(epd);

		// bug #59, remove empty commissioner and goal types
		if (adminInfo != null && adminInfo.getCommissionerAndGoal() != null) {
			var comGoal = adminInfo.getCommissionerAndGoal();
			if (isEmpty(comGoal.getOther())) {
				comGoal.withOther(null);
			}
			if (isEmpty(comGoal)) {
				adminInfo.withCommissionerAndGoal(null);
			}
		}

		if (adminInfo != null && isEmpty(adminInfo.getDataGenerator())) {
			adminInfo.withDataGenerator(null);
		}
	}

	private static boolean isEmpty(CommissionerAndGoal comGoal) {
		if (comGoal == null)
			return true;
		return comGoal.getCommissioners().isEmpty()
			&& comGoal.getIntendedApplications().isEmpty()
			&& comGoal.getProject().isEmpty()
			&& isEmpty(comGoal.getOther());
	}

	private static boolean isEmpty(Time time) {
		if (time == null)
			return true;
		return time.getDescription().isEmpty()
			&& time.getReferenceYear() == null
			&& time.getValidUntil() == null
			&& isEmpty(time.getEpdExtension());
	}

	private static boolean isEmpty(Geography geography) {
		if (geography == null)
			return true;
		return isEmpty(geography.getLocation())
			&& geography.getSubLocations().isEmpty()
			&& isEmpty(geography.getOther());
	}

	private static boolean isEmpty(Technology technology) {
		if (technology == null)
			return true;
		return technology.getApplicability().isEmpty()
			&& technology.getDescription().isEmpty()
			&& technology.getIncludedProcesses().isEmpty()
			&& technology.getPictogram() == null
			&& technology.getPictures().isEmpty();
	}

	private static boolean isEmpty(Location location) {
		if (location == null)
			return true;
		return Strings.isNullOrEmpty(location.getCode())
			&& location.getDescription().isEmpty()
			&& isEmpty(location.getOther());
	}

	private static boolean isEmpty(DataGenerator generator) {
		if (generator == null)
			return true;
		return generator.getContacts().isEmpty()
			&& isEmpty(generator.getOther());
	}

	private static boolean isEmpty(EpdTimeExtension ext) {
		if (ext == null)
			return true;
		return ext.getPublicationDate() == null
			&& ext.getAny().isEmpty();
	}

	private static boolean isEmpty(Other other) {
		return other == null || other.getAny().isEmpty();
	}

	static boolean isEmpty(EpdInfoExtension ext) {
		if (ext == null)
			return true;
		return isEmpty(ext.getContentDeclaration())
			&& ext.getModuleEntries().isEmpty()
			&& ext.getScenarios().isEmpty()
			&& isEmpty(ext.getSafetyMargins())
			&& ext.getAny().isEmpty();
	}

	private static boolean isEmpty(EpdContentDeclaration dec) {
		return dec == null || dec.getElements().isEmpty();
	}

	private static boolean isEmpty(EpdSafetyMargins esm) {
		if (esm == null)
			return true;
		return esm.getValue() == null && esm.getDescription().isEmpty();
	}

}
