package epd.io.conversion;

import java.util.List;

import org.openlca.ilcd.processes.Process;

import epd.model.EpdDataSet;
import epd.model.IndicatorMapping;

public class Extensions {

	static final String NS_EPD = "http://www.iai.kit.edu/EPD/2013";
	static final String NS_OLCA = "http://openlca.org/epd_ilcd";
	static final String NS_XML = "http://www.w3.org/XML/1998/namespace";

	private Extensions() {
	}

	public static EpdDataSet read(Process process,
			List<IndicatorMapping> indicators) {
		return new ProcessConverter(process, indicators).convert();
	}

	public static void write(EpdDataSet dataSet,
			List<IndicatorMapping> indicators) {
		new EpdConverter(dataSet, indicators).convert();
	}

}
