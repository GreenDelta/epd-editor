package epd.model;

import java.util.Objects;

public class ModuleEntry {

	public Module module;
	public String productSystemId;
	public String scenario;
	public String description;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ModuleEntry))
			return false;
		ModuleEntry other = (ModuleEntry) obj;
		return Objects.equals(this.module, other.module)
				&& Objects.equals(this.scenario, other.scenario)
				&& Objects.equals(this.productSystemId, other.productSystemId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(module, scenario, productSystemId);
	}

	@Override
	public String toString() {
		return "ModuleEntry [module=" + module + ", scenario=" + scenario + "]";
	}

	@Override
	public ModuleEntry clone() {
		ModuleEntry clone = new ModuleEntry();
		clone.module = module;
		clone.productSystemId = productSystemId;
		clone.scenario = scenario;
		clone.description = description;
		return clone;
	}
}
