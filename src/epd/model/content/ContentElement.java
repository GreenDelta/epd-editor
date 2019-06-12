package epd.model.content;

import epd.model.dom.Elem;
import epd.model.dom.NS;

public abstract class ContentElement {

	/** Name of the component, material, or substance. */
	public String name;

	/**
	 * Mass percentage: either a discrete value or a range of values has to be
	 * specified.
	 */
	@Elem(qname = "epd2:weightPerc", namespace = NS.EPDv2)
	public ContentAmount massPerc;

	/**
	 * Absolute mass of the fraction in kg. Either a discrete value or a range
	 * of values has to be specified.
	 */
	@Elem(qname = "epd2:mass", namespace = NS.EPDv2)
	public ContentAmount mass;

	/** Some comment about the component, material, or substance. */
	public String comment;

}
