package epd.model.content;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import epd.io.conversion.Dom;

public class Component extends ContentElement {

	/** A component can contain materials and substances. */
	public final List<ContentElement> content = new ArrayList<>();

	@Override
	Component read(Element e) {
		if (e == null)
			return this;
		super.read(e);
		Dom.eachChild(e, child -> {
			ContentElement ce = ContentDeclaration.makeElement(child);
			if ((ce instanceof Material) || (ce instanceof Substance)) {
				content.add(ce);
			}
		});
		return this;
	}

	@Override
	public Component clone() {
		Component clone = new Component();
		copyTo(clone);
		for (ContentElement e : content) {
			if (e != null) {
				clone.content.add(e.clone());
			}
		}
		return clone;
	}
}
