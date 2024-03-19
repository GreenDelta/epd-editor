package epd.model;

import org.openlca.ilcd.commons.Copyable;

import java.util.Objects;

public final class MaterialProperty implements Copyable<MaterialProperty> {

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
		if (!(obj instanceof MaterialProperty other))
			return false;
		return Objects.equals(this.id, other.id);
	}

	@Override
	public int hashCode() {
		return id == null
			? super.hashCode()
			: id.hashCode();
	}

	@Override
	public MaterialProperty copy() {
		var copy = new MaterialProperty();
		copy.id = id;
		copy.name = name;
		copy.unit = unit;
		copy.unitDescription = unitDescription;
		return copy;
	}
}
