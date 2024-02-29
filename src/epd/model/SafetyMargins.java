package epd.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Copyable;
import org.openlca.ilcd.commons.LangString;

public class SafetyMargins implements Copyable<SafetyMargins> {

	public Double margins;
	public final List<LangString> description = new ArrayList<>();

	@Override
	public SafetyMargins copy() {
		var clone = new SafetyMargins();
		clone.margins = margins;
		for (LangString d : description) {
			if (d != null) {
				clone.description.add(d.copy());
			}
		}
		return clone;
	}
}
