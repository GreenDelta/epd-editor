package epd.profiles;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.EpdIndicatorResult;

import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class Indicator {

	@XmlAttribute(name = "isInputIndicator", namespace = EpdProfile2.NS)
	private Boolean inputIndicator;

	@XmlAttribute(name = "group", namespace = EpdProfile2.NS)
	private String group;

	@XmlElement(name = "ref", namespace = EpdProfile2.NS)
	private Ref ref;

	@XmlElement(name = "unitGroup", namespace = EpdProfile2.NS)
	private Ref unit;

	public boolean isInputIndicator() {
		return inputIndicator != null && inputIndicator;
	}

	public boolean isInventoryIndicator() {
		return ref != null && ref.getType() == DataSetType.FLOW;
	}

	public String getGroup() {
		return group;
	}

	public Ref getRef() {
		return ref;
	}

	public Ref getUnit() {
		return unit;
	}

	public String getUUID() {
		return ref != null ? ref.getUUID() : null;
	}

	public Indicator withGroup(String group) {
		this.group = group;
		return this;
	}

	public Indicator withInputIndicator(Boolean inputIndicator) {
		this.inputIndicator = inputIndicator;
		return this;
	}

	public Indicator withRef(Ref ref) {
		this.ref = ref;
		return this;
	}

	public Indicator withUnit(Ref unit) {
		this.unit = unit;
		return this;
	}

	@Override
	public int hashCode() {
		var id = getUUID();
		return id != null ? id.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		return obj instanceof Indicator other
			&& Objects.equals(this.getUUID(), other.getUUID());
	}

	@Override
	public String toString() {
		var name = ref != null
			? LangString.getFirst(ref.getName())
			: null;
		return "Indicator [ name=\"" + name + "\" uuid =\"" + getUUID() + "\"]";
	}

	public EpdIndicatorResult createResult() {
		if (isInventoryIndicator())
			return isInputIndicator()
				? EpdIndicatorResult.ofInputIndicator(ref, unit)
				: EpdIndicatorResult.ofOutputIndicator(ref, unit);
		return EpdIndicatorResult.of(ref, unit);
	}
}
