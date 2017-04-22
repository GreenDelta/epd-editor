package app.rcp;

import org.openlca.ilcd.commons.DataSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import epd.model.Indicator;
import epd.model.IndicatorGroup;
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

	public static String get(Indicator indicator) {
		if (indicator == null)
			return "unknown";
		String name = indicator.name();
		String field = "Indicator_" + name;
		try {
			Object val = M.class.getField(field).get(null);
			return val != null ? val.toString() : "unknown";
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Labels.class);
			log.error("failed to get indicator label: " + field, e);
			return "unknown";
		}
	}

	public static String get(IndicatorGroup group) {
		switch (group) {
		case ENVIRONMENTAL:
			return M.EnvironmentalParameters;
		case OUTPUT_FLOWS:
			return M.OutputParameters;
		case RESOURCE_USE:
			return M.ResourceParameters;
		case WASTE_DISPOSAL:
			return M.WasteParameters;
		default:
			return M.Unknown;
		}
	}
}
