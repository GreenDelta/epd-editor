package epd.model.content;

public class Substance extends ContentElement {

	/** The optional data dictionary GUID (whatever this is). */
	public String guid;

	/** CAS Number of the material or substance. */
	public String casNumber;

	/** EC Number of the material or substance. */
	public String ecNumber;

	/** The percentage of renewable resources contained. */
	public Double renewable;

	/** The percentage of recycled materials contained. */
	public Double recycled;

	/** The percentage of recycled materials contained. */
	public Double recyclable;

	public Boolean packaging;
}
