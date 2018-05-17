package epd.model;

/**
 * Each indicator contains the field values that are necessary for serializing
 * the indicator results into the extended ILCD format. Inventory indicators are
 * serialized as exchanges and impact assessment indicators are serialized as
 * LCIA results in an ILCD data set.
 */
public class Indicator {

	public enum Type {
		LCI, LCIA
	}

	/**
	 * The default name of the indicator as loaded from an EPD profile. Note
	 * that the name for the user interface and serialization should be taken
	 * from the respective indicator data set (if available).
	 */
	public String name;

	public Type type;

	public String group;

	public String unit;

	/** The UUID of the ILCD indicator data set. */
	public String indicatorRef;

	/**
	 * The UUID of the ILCD unit group data set. The reference unit of this unit
	 * group data set is the unit in which is indicator is quantified.
	 */
	public String unitRef;

}
