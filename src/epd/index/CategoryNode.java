package epd.index;

import java.util.Objects;

import org.openlca.ilcd.commons.Category;

import epd.util.Strings;

public class CategoryNode extends Node {

	public Category category;

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CategoryNode other))
			return false;
		var c1 = this.category;
		var c2 = other.category;
		if (c1 == null && c2 == null)
			return true;
		if (c1 == null || c2 == null)
			return false;
		if (c1.getLevel() != c2.getLevel())
			return false;
		if (!Strings.nullOrEqual(c1.getClassId(), c2.getClassId()))
			return false;
		return Objects.equals(c1.getName(), c2.getName());
	}
}
