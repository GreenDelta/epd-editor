package epd.model.content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.ilcd.commons.LangString;
import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;

public abstract class ContentElement {

	/** Name of the component, material, or substance. */
	public final List<LangString> name = new ArrayList<>();

	/**
	 * Mass percentage: either a discrete value or a range of values has to be
	 * specified.
	 */
	public ContentAmount massPerc;

	/**
	 * Absolute mass of the fraction in kg. Either a discrete value or a range
	 * of values has to be specified.
	 */
	public ContentAmount mass;

	/** Some comment about the component, material, or substance. */
	public final List<LangString> comment = new ArrayList<>();

	ContentElement read(Element e) {
		if (e == null)
			return this;
		name.addAll(Dom.getChilds(e, "name", Vocab.NS_EPDv2)
				.stream().map(Dom::getLangString)
				.filter(n -> n != null)
				.collect(Collectors.toList()));
		comment.addAll(Dom.getChilds(e, "comment", Vocab.NS_EPDv2)
				.stream().map(Dom::getLangString)
				.filter(n -> n != null)
				.collect(Collectors.toList()));

		Element massPercElem = Dom.getChild(e, "weightPerc", Vocab.NS_EPDv2);
		if (massPercElem != null) {
			massPerc = ContentAmount.from(massPercElem);
		}
		Element massElem = Dom.getChild(e, "mass", Vocab.NS_EPDv2);
		if (massElem != null) {
			mass = ContentAmount.from(massElem);
		}
		return this;
	}

	void write(Element elem) {
		if (elem == null)
			return;
		// TODO
	}

	void copyTo(ContentElement other) {
		if (other == null)
			return;
		for (LangString n : name) {
			other.name.add(n.clone());
		}
		if (massPerc != null) {
			other.massPerc = massPerc.clone();
		}
		if (mass != null) {
			other.mass = mass.clone();
		}
		for (LangString c : comment) {
			other.comment.add(c.clone());
		}
	}

	@Override
	public abstract ContentElement clone();
}
