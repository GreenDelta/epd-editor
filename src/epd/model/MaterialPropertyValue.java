package epd.model;

public class MaterialPropertyValue {

	public MaterialProperty property;
	public double value;

	@Override
	public MaterialPropertyValue clone() {
		MaterialPropertyValue clone = new MaterialPropertyValue();
		clone.property = property;
		clone.value = value;
		return clone;
	}
}
