package epd.model.content;

import java.util.ArrayList;
import java.util.List;

import epd.model.dom.Elem;
import epd.model.dom.NS;

@Elem(qname = "epd2:component", namespace = NS.EPDv2)
public class Component extends ContentElement {

	/**
	 * A component can contain materials and substances.
	 */
	public final List<ContentElement> content = new ArrayList<>();

}
