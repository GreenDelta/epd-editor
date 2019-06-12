package epd.model.content;

public abstract class ContentElement {

	/** Name of the component, material, or substance. */
	public String name;

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
	public String comment;

}
