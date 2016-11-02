package epd.model;

public enum Module {

	A1("A1"),

	A2("A2"),

	A3("A3"),

	A1_A3("A1-A3"),

	A4("A4"),

	A5("A5"),

	B1("B1"),

	B2("B2"),

	B3("B3"),

	B4("B4"),

	B5("B5"),

	B6("B6"),

	B7("B7"),

	C1("C1"),

	C2("C2"),

	C3("C3"),

	C4("C4"),

	D("D");

	private final String label;

	private Module(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static Module fromLabel(String label) {
		if (label == null)
			return null;
		String l = label.trim();
		for (Module type : values()) {
			if (type.label.equalsIgnoreCase(l))
				return type;
		}
		return null;
	}

	@Override
	public String toString() {
		return label;
	}

}
