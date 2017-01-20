package app.rcp;

import org.openlca.ilcd.commons.DataSetType;

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
			return "#Contact";
		case EXTERNAL_FILE:
			return "#External File";
		case FLOW:
			return "#Flow";
		case FLOW_PROPERTY:
			return "#Flow Property";
		case LCIA_METHOD:
			return "#LCIA Method";
		case PROCESS:
			return "#EPD Data Set";
		case SOURCE:
			return "#Source";
		case UNIT_GROUP:
			return "#Unit Group";
		default:
			return "#Unknown";
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
		String key = "Indicator_" + name;
		String val = M.getMap().get(key);
		return val == null ? "unknown" : val;
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
			return "#unknown";
		}
	}
}
