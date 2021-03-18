package epd.io.conversion;

import epd.model.EpdDataSet;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.util.Processes;

/**
 * Remove empty elements so that the data set validation is happy.
 */
class Cleanup {

	static void on(EpdDataSet epd) {
		if (epd == null)
			return;
		var adminInfo = Processes.getAdminInfo(epd.process);

		// bug #59, remove empty commissioner and goal types
		if (adminInfo != null && adminInfo.commissionerAndGoal != null) {
			var comGoal = adminInfo.commissionerAndGoal;
			if (isEmpty(comGoal.other)) {
				comGoal.other = null;
			}
			if (isEmpty(comGoal)) {
				adminInfo.commissionerAndGoal = null;
			}
		}

	}

	private static boolean isEmpty(Other other) {
		return other == null || other.any.isEmpty();
	}

	private static boolean isEmpty(CommissionerAndGoal comGoal) {
		if (comGoal == null)
			return true;
		return comGoal.commissioners.isEmpty()
			&& comGoal.intendedApplications.isEmpty()
			&& comGoal.project.isEmpty()
			&& isEmpty(comGoal.other);
	}
}
