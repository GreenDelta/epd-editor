package epd.model.content;

import epd.model.dom.Attr;
import epd.model.dom.NS;

public class ContentAmount {

	/** For specifying a discrete value: the value. */
	@Attr(qname = "epd2:value", namespace = NS.EPDv2)
	public Double value;

	/** For specifying a range of values: the lower value of the range. */
	@Attr(qname = "epd2:lowerValue", namespace = NS.EPDv2)
	public Double lowerValue;

	/**
	 * For specifying a range of values: the upper value of the range. For
	 * specifying a value lower than x (e.g. "<42"), only specify the upper
	 * value as x.
	 */
	@Attr(qname = "epd2:upperValue", namespace = NS.EPDv2)
	public Double upperValue;

}
