package epd.model;


import java.util.Locale;
import java.util.Objects;

import org.openlca.commons.Copyable;

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

	/**
	 * Returns true when the given property describes the same physical property
	 * as this one. The ID of a material property is only unique within a single
	 * MatML document (it is an xsd:ID there), so properties from different
	 * sources (e.g. the local property store and an imported flow) are compared
	 * by name and unit and not by ID:
	 * <ul>
	 * <li>names are compared case-insensitively, ignoring surrounding and
	 * repeated whitespace</li>
	 * <li>units are compared case-sensitively (e.g. {@code Mg} is not
	 * {@code mg}), ignoring whitespace and exponent notation, so that
	 * {@code kg/m^3}, {@code kg/m3}, and {@code kg/m³} are the same unit</li>
	 * <li>a property without a name is only the same as itself</li>
	 * </ul>
	 */
	public boolean isSameAs(MaterialProperty other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		var name = normalizeName(this.name);
		if (name.isEmpty() || !name.equals(normalizeName(other.name)))
			return false;
		return normalizeUnit(this.unit).equals(normalizeUnit(other.unit));
	}

	private static String normalizeName(String s) {
		return s == null
			? ""
			: s.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

	private static String normalizeUnit(String s) {
		if (s == null)
			return "";
		var b = new StringBuilder();
		for (var c : s.toCharArray()) {
			switch (c) {
				case '^' -> {
				}
				case '¹' -> b.append('1');
				case '²' -> b.append('2');
				case '³' -> b.append('3');
				default -> {
					if (!Character.isWhitespace(c)) {
						b.append(c);
					}
				}
			}
		}
		return b.toString();
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
