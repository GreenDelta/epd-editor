package epd.model.content;

import java.io.File;

import org.openlca.ilcd.io.XmlBinder;
import org.openlca.ilcd.processes.Process;

import epd.io.conversion.Extensions;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;

public class Main {
	public static void main(String[] args) throws Exception {
		String path = "C:\\Users\\ms\\Projects\\_current\\epd_editor\\sample_EPD.xml";
		XmlBinder binder = new XmlBinder();
		Process p = binder.fromFile(Process.class, new File(path));
		EpdDataSet ds = Extensions.read(p, new EpdProfile());
	}
}
