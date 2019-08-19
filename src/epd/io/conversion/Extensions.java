package epd.io.conversion;

import org.openlca.ilcd.processes.Process;

import epd.model.EpdDataSet;
import epd.model.EpdProfile;

public class Extensions {

	private Extensions() {
	}

	public static EpdDataSet read(Process process, EpdProfile profile) {
		return new ProcessConverter(process, profile).convert();
	}

	public static void write(EpdDataSet dataSet) {
		new EpdConverter(dataSet).convert();
	}

}
