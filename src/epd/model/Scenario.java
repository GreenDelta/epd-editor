package epd.model;

public class Scenario {

	public String name;
	public boolean defaultScenario;
	public String group;
	public String description;

	@Override
	public Scenario clone() {
		Scenario clone = new Scenario();
		clone.name = name;
		clone.defaultScenario = defaultScenario;
		clone.group = group;
		clone.description = description;
		return clone;
	}
}
