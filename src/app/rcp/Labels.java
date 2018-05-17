package app.rcp;

import org.openlca.ilcd.commons.DataSetType;

import app.M;
import epd.model.SubType;

public class Labels {

	private Labels() {
	}

	public static String get(DataSetType type) {
		if (type == null)
			return "";
		switch (type) {
		case CONTACT:
			return M.Contact;
		case EXTERNAL_FILE:
			return M.ExternalFile;
		case FLOW:
			return M.Flow;
		case FLOW_PROPERTY:
			return M.FlowProperty;
		case LCIA_METHOD:
			return M.LCIAMethod;
		case PROCESS:
			return M.EPD;
		case SOURCE:
			return M.Source;
		case UNIT_GROUP:
			return M.UnitGroup;
		default:
			return M.Unknown;
		}
	}

	public static String get(SubType subtype) {
		if (subtype == null)
			return null;
		switch (subtype) {
		case AVERAGE:
			return M.Average;
		case GENERIC:
			return M.Generic;
		case REPRESENTATIVE:
			return M.Representative;
		case SPECIFIC:
			return M.Specific;
		case TEMPLATE:
			return M.Template;
		default:
			return M.None;
		}
	}

}
