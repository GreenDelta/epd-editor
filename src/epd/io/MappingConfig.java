package epd.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import epd.model.Indicator;

/**
 * Configuration for mapping EPD data sets to ILCD and back again.
 */
public class MappingConfig {

	public String impactMethodRefId;
	public String impactMethodName;
	public final List<IndicatorMapping> indicatorMappings = new ArrayList<>();

	public IndicatorMapping getIndicatorMapping(Indicator indicator) {
		if (indicator == null)
			return null;
		for (IndicatorMapping mapping : indicatorMappings) {
			if (mapping.indicator == indicator)
				return mapping;
		}
		return null;
	}

	public Indicator getIndicator(String refId) {
		if (refId == null)
			return null;
		for (IndicatorMapping mapping : indicatorMappings) {
			if (Objects.equals(mapping.indicatorRefId, refId))
				return mapping.indicator;
		}
		return null;
	}
}
