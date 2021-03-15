package epd.io.conversion;

import org.openlca.ilcd.processes.Process;

import epd.model.EpdDataSet;
import epd.model.EpdProfile;

public class Extensions {

	private Extensions() {
	}

	/**
	 * Reads the extensions of the given process data set into an EPD data set.
	 * The given process data set will be wrapped by the EPD data set.
	 */
	public static EpdDataSet read(Process process, EpdProfile profile) {
		return new ProcessConverter(process, profile).convert();
	}

	/**
	 * Write the EPD extensions into the underlying process data set of the
	 * given EPD.
	 */
	public static void write(EpdDataSet epd) {
		new EpdConverter(epd).convert();
	}
}
