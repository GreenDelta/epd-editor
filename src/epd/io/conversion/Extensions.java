package epd.io.conversion;

import org.openlca.ilcd.processes.Process;

import epd.model.EpdDataSet;
import epd.model.EpdProfile;

public class Extensions {

	static final String NS_EPD = "http://www.iai.kit.edu/EPD/2013";
	static final String NS_OLCA = "http://openlca.org/epd_ilcd";
	static final String NS_XML = "http://www.w3.org/XML/1998/namespace";

	private Extensions() {
	}

	public static EpdDataSet read(Process process, EpdProfile profile) {
		return new ProcessConverter(process, profile).convert();
	}

	public static void write(EpdDataSet dataSet) {
		new EpdConverter(dataSet).convert();
	}

}
