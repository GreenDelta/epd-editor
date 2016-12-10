package epd.io.conversion;

import org.openlca.ilcd.processes.Process;

import epd.io.MappingConfig;
import epd.model.EpdDataSet;

public class Converter {

	public static String NAMESPACE = "http://www.iai.kit.edu/EPD/2013";
	public static String NAMESPACE_OLCA = "http://openlca.org/epd_ilcd";

	private Converter() {
	}

	public static EpdDataSet convert(Process process, MappingConfig config,
			String[] langs) {
		return new ProcessConverter(process, config).convert(langs);
	}

	public static Process convert(EpdDataSet dataSet, MappingConfig config) {
		return new EpdConverter(dataSet, config).convert();
	}

}
