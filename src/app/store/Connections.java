package app.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.io.SodaConnection;

import app.App;
import epd.util.Strings;

public class Connections {

	public static void save(SodaConnection con) {
		if (con == null || con.uuid == null)
			return;
		File f = new File(dir(), con.uuid + ".json");
		Json.write(con, f);
	}

	public static List<SodaConnection> get() {
		List<SodaConnection> list = new ArrayList<>();
		for (File f : dir().listFiles()) {
			SodaConnection con = Json.read(f, SodaConnection.class);
			if (con != null)
				list.add(con);
		}
		Collections.sort(list,
				(c1, c2) -> Strings.compare(c1.toString(), c2.toString()));
		return list;
	}

	public static void delete(SodaConnection con) {
		if (con == null)
			return;
		File f = new File(dir(), con.uuid + ".json");
		if (f.exists())
			f.delete();
	}

	private static File dir() {
		File dir = new File(App.workspace, "connections");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

}
