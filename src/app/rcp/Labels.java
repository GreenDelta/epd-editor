package app.rcp;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.epd.EpdSubType;

import app.App;
import app.M;
import epd.util.Strings;

public class Labels {

	private Labels() {
	}

	public static String get(Category c) {
		return c != null
			? ofCategory(c.getClassId(), c.getName())
			: null;
	}

	public static String get(org.openlca.ilcd.lists.Category c) {
		return c != null
			? ofCategory(c.getId(), c.getName())
			: null;
	}

	private static String ofCategory(String id, String name) {
		if (Strings.nullOrEmpty(name))
			return id;
		if (Strings.nullOrEmpty(id)
			|| id.length() > 10
			|| App.settings().hideCategoryIds)
			return name;
		return id + " " + name;
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
