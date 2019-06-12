package epd.model.content;

public class ContentAmount {

	/** For specifying a discrete value: the value. */
	public Double value;

	/** For specifying a range of values: the lower value of the range. */
	public Double lowerValue;

	/**
	 * For specifying a range of values: the upper value of the range. For
	 * specifying a value lower than x (e.g. "<42"), only specify the upper
	 * value as x.
	 */
	public Double upperValue;

}
