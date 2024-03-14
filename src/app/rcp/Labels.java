package app.rcp;

import app.M;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.epd.EpdSubType;

public class Labels {

	private Labels() {
	}

	public static String get(DataSetType type) {
		if (type == null)
			return "";
		return switch (type) {
			case CONTACT -> M.Contact;
			case EXTERNAL_FILE -> M.ExternalFile;
			case FLOW -> M.Flow;
			case FLOW_PROPERTY -> M.FlowProperty;
			case IMPACT_METHOD -> M.LCIAMethod;
			case PROCESS -> M.EPD;
			case SOURCE -> M.Source;
			case UNIT_GROUP -> M.UnitGroup;
			default -> M.Unknown;
		};
	}

	public static String get(EpdSubType subtype) {
		if (subtype == null)
			return null;
		return switch (subtype) {
			case AVERAGE_DATASET -> M.Average;
			case GENERIC_DATASET -> M.Generic;
			case REPRESENTATIVE_DATASET -> M.Representative;
			case SPECIFIC_DATASET -> M.Specific;
			case TEMPLATE_DATASET -> M.Template;
		};
	}

}
