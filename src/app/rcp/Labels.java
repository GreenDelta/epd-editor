package app.rcp;

import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.epd.EpdManufacturerVariability;
import org.openlca.ilcd.processes.epd.EpdProductVariability;
import org.openlca.ilcd.processes.epd.EpdSubType;
import org.openlca.ilcd.processes.epd.EpdVariationRange;
import org.openlca.ilcd.sources.SourceType;

import app.App;
import app.M;

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
		if (Strings.isBlank(name))
			return id;
		if (Strings.isBlank(id)
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

	public static String get(SourceType type) {
		if (type == null)
			return "";
		return switch (type) {
			case ARTICLE_IN_PERIODICAL -> M.ArticleInPeriodical;
			case CHAPTER_IN_ANTHOLOGY -> M.ChapterInAnthology;
			case DIRECT_MEASUREMENT -> M.DirectMeasurement;
			case MONOGRAPH -> M.Monograph;
			case ORAL_COMMUNICATION -> M.OralCommunication;
			case PERSONAL_WRITTEN_COMMUNICATION ->
				M.PersonalWrittenCommunication;
			case QUESTIONNAIRE -> M.Questionnaire;
			case SOFTWARE_OR_DATABASE -> M.SoftwareOrDatabase;
			case OTHER_UNPUBLISHED_AND_GREY_LITERATURE ->
				M.OtherUnpublishedAndGreyLiterature;
			case UNDEFINED -> M.Undefined;
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

	public static String get(EpdManufacturerVariability.VariabilityType t) {
		if (t == null)
			return "";
		return switch (t) {
			case SINGLE_PRODUCTION_SITE -> M.SingleProductionSite;
			case SINGLE_MANUFACTURER_MULTIPLE_SITES -> M.SingleManufacturerMultipleSites;
			case MULTIPLE_MANUFACTURERS -> M.MultipleManufacturers;
		};
	}

	public static String get(EpdProductVariability.VariabilityType t) {
		if (t == null)
			return "";
		return switch (t) {
			case SINGLE_PRODUCT -> M.SingleProduct;
			case RANGE_OF_PRODUCTS -> M.RangeOfProducts;
		};
	}

	public static String get(EpdVariationRange range) {
		if (range == null)
			return "";
		return switch (range) {
			case A_LESS_THAN_2_5 -> M.VariationRangeA;
			case B_BETWEEN_2_5_AND_10 -> M.VariationRangeB;
			case C_BETWEEN_10_AND_25 -> M.VariationRangeC;
			case D_BETWEEN_25_AND_50 -> M.VariationRangeD;
			case E_MORE_THAN_50 -> M.VariationRangeE;
		};
	}

}
