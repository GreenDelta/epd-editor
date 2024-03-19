package epd.model;

import org.openlca.ilcd.commons.Copyable;

public final class MaterialPropertyValue implements Copyable<MaterialPropertyValue> {

	public MaterialProperty property;
	public double value;

	@Override
	public MaterialPropertyValue copy() {
		var copy = new MaterialPropertyValue();
		copy.property = property;
		copy.value = value;
		return copy;
	}
}
