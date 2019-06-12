package epd.model.content;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import epd.io.conversion.Dom;

/**
 * In the logic of the EPD editor, a material is the same as a substance but it
 * can contain other substances. In this sense it extends a substance.
 */
public class Material extends Substance {

	public final List<Substance> substances = new ArrayList<>();

	@Override
	Material read(Element e) {
		if (e == null)
			return this;
		super.read(e);
		Dom.eachChild(e, child -> {
			ContentElement ce = ContentDeclaration.makeElement(child);
			if (ce instanceof Substance) {
				substances.add((Substance) ce);
			}
		});
		return this;
	}
}
