package epd.io;

import epd.model.Indicator;

public class IndicatorMapping {

	public Indicator indicator;
	public String indicatorRefId;
	public String indicatorLabel;
	public String unitRefId;
	public String unitLabel;

	@Override
	public String toString() {
		return "IndicatorMapping [indicator=" + indicator + ", indicatorRefId="
				+ indicatorRefId + ", indicatorLabel=" + indicatorLabel + "]";
	}
}
