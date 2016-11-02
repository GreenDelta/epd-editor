package epd.model;

import java.util.Objects;

public class MaterialProperty {

	public String id;
	public String name;
	public String unit;
	public String unitDescription;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof MaterialProperty))
			return false;
		MaterialProperty other = (MaterialProperty) obj;
		return Objects.equals(this.id, other.id);
	}

	@Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}

	@Override
	public MaterialProperty clone() {
		MaterialProperty clone = new MaterialProperty();
		clone.id = id;
		clone.name = name;
		clone.unit = unit;
		clone.unitDescription = unitDescription;
		return clone;
	}
}