package epd.model;

import java.util.Objects;

import epd.util.Strings;

public class Module {

	public int index;
	public String name;

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Module))
			return false;
		Module other = (Module) obj;
		return Strings.nullOrEqual(this.name, other.name);
	}

}
