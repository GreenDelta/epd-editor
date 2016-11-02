package epd.model;

public class SafetyMargins {

	public Double margins;
	public String description;

	@Override
	public SafetyMargins clone() {
		SafetyMargins clone = new SafetyMargins();
		clone.margins = margins;
		clone.description = description;
		return clone;
	}
}
