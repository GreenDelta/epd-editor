package epd.io.conversion;

import java.util.List;

import org.openlca.ilcd.processes.Process;

import epd.model.EpdDataSet;
import epd.model.IndicatorMapping;

public class ProcessExtensions {

	static String NAMESPACE = "http://www.iai.kit.edu/EPD/2013";
	static String NAMESPACE_OLCA = "http://openlca.org/epd_ilcd";

	private ProcessExtensions() {
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
