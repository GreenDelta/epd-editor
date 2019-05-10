package epd.model.content;

import epd.model.dom.Elem;
import epd.model.dom.NS;

@Elem(qname = "epd2:component", namespace = NS.EPDv2)
public class Component {

	/**
	 * Mass percentage: either a discrete value or a range of values has to be
	 * specified.
	 */
	@Elem(qname = "epd2:weightPerc", namespace = NS.EPDv2)
	public ContentValue massPerc;

	/**
	 * Absolute mass of the fraction in kg. Either a discrete value or a range
	 * of values has to be specified.
	 */
	@Elem(qname = "epd2:mass", namespace = NS.EPDv2)
	public ContentValue mass;
}
