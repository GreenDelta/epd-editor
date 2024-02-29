package app.rcp;

import app.M;
import epd.model.SubType;
import org.openlca.ilcd.commons.DataSetType;

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

	public static String get(SubType subtype) {
		if (subtype == null)
			return null;
		return switch (subtype) {
			case AVERAGE -> M.Average;
			case GENERIC -> M.Generic;
			case REPRESENTATIVE -> M.Representative;
			case SPECIFIC -> M.Specific;
			case TEMPLATE -> M.Template;
		};
	}

}
