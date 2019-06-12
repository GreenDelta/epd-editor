package epd.model.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.w3c.dom.Element;

import epd.io.conversion.Dom;
import epd.io.conversion.Vocab;

/**
 * Content declaration according to EN 15804/ISO 219301. The content declaration
 * may contain component, material and/or substance elements, which may (but do
 * not have to) be nested.
 */
public class ContentDeclaration {

	/**
	 * A content declaration can contain components, materials, and substances.
	 * Components can in turn contain materials and substances; materials can
	 * contain substances.
	 */
	public final List<ContentElement> content = new ArrayList<>();

	public static ContentDeclaration fromXml(Other other) {
		if (other == null)
			return null;

		// find the root element
		Element root = null;
		for (Object any : other.any) {
			if (!(any instanceof Element))
				continue;
			Element e = (Element) any;
			if (Objects.equals(Vocab.NS_EPDv2, e.getNamespaceURI())
					&& Objects.equals("contentDeclaration", e.getLocalName())) {
				root = e;
				break;
			}
		}
		if (root == null)
			return null;

		Dom.eachChild(root, e -> {
			ContentElement ce = makeElement(e);
			if (ce == null)
				return;

			System.out.println(ce.name);
		});

		return null;
	}

	static ContentElement makeElement(Element elem) {
		if (elem == null)
			return null;
		if (!Objects.equals(Vocab.NS_EPDv2, elem.getNamespaceURI()))
			return null;
		switch (elem.getLocalName()) {
		case "component":
			return new Component().read(elem);
		case "material":
			return new Material().read(elem);
		case "substance":
			return new Substance().read(elem);
		default:
			return null;
		}
	}
}
