package epd.profiles;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.EpdIndicatorResult;

import java.util.Objects;

/**
 * Each indicator contains the field values that are necessary for serializing
 * the indicator results into the extended ILCD format. Inventory indicators are
 * serialized as exchanges and impact assessment indicators are serialized as
 * LCIA results in an ILCD data set.
 */
public final class Indicator {

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

	/**
	 * This is only relevant for LCI indicators which are exported as exchanges
	 * in the ILCD format. An LCI indicator is an input indicator if and only if
	 * this field has the value `true`.
	 */
	public Boolean isInput;

	public String group;

	public String unit;

	/**
	 * The UUID of the ILCD indicator data set.
	 */
	public String uuid;

	/**
	 * The UUID of the ILCD unit group data set. The reference unit of this unit
	 * group data set is the unit in which is indicator is quantified.
	 */
	public String unitGroupUUID;

	/**
	 * Create a ILCD data set reference for this indicator.
	 */
	public Ref getRef(String lang) {
		String path = type == Type.LCI
			? "flows"
			: "lciamethods";
		Ref ref = new Ref()
			.withUUID(uuid)
			.withUri("../" + path + "/" + uuid)
			.withType(type == Type.LCI
				? DataSetType.FLOW
				: DataSetType.IMPACT_METHOD);
		LangString.set(ref.withName(), name, lang);
		return ref;
	}

	/**
	 * Create a ILCD data set reference to the unit group of this indicator.
	 */
	public Ref getUnitGroupRef(String lang) {
		Ref ref = new Ref()
			.withUUID(unitGroupUUID)
			.withType(DataSetType.UNIT_GROUP)
			.withUri("../unitgroups/" + unitGroupUUID);
		LangString.set(ref.withName(), unit, lang);
		return ref;
	}

	@Override
	public int hashCode() {
		if (uuid == null)
			return super.hashCode();
		return uuid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Indicator other))
			return false;
		return Objects.equals(this.uuid, other.uuid);
	}

	@Override
	public String toString() {
		return "Indicator [ name=\"" + name
			+ "\" uuid =\"" + uuid + "\"]";
	}

	public EpdIndicatorResult createResult(String lang) {
		var indicatorRef = getRef(lang);
		var unitGroupRef = getUnitGroupRef(lang);
		if (type == Type.LCIA)
			return EpdIndicatorResult.of(indicatorRef, unitGroupRef);
		return isInput != null && isInput
			? EpdIndicatorResult.ofInputIndicator(indicatorRef, unitGroupRef)
			: EpdIndicatorResult.ofOutputIndicator(indicatorRef, unitGroupRef);
	}

}
