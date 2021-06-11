package app.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.io.SodaConnection;

import app.App;
import app.editors.Editors;
import app.navi.Navigator;
import epd.util.Strings;
import org.slf4j.LoggerFactory;

public class Connections {

	public static void save(SodaConnection con) {
		if (con == null || con.uuid == null)
			return;
		File f = new File(dir(), con.uuid + ".json");
		Json.write(con, f);
		Navigator.refreshConnections();
	}

	public static List<SodaConnection> get() {
		var files = dir().listFiles();
		if (files == null)
			return Collections.emptyList();
		var list = new ArrayList<SodaConnection>();
		for (File f : files) {
			var con = Json.read(f, SodaConnection.class);
			if (con != null) {
				list.add(con);
			}
		}
		list.sort((c1, c2) -> Strings.compare(c1.toString(), c2.toString()));
		return list;
	}

	public static void delete(SodaConnection con) {
		if (con == null)
			return;
		File f = new File(dir(), con.uuid + ".json");
		try {
			Files.delete(f.toPath());
		} catch (IOException e) {
			var log = LoggerFactory.getLogger(Connections.class);
			log.error("failed to delete connection: " + f, e);
		}
		Navigator.refreshConnections();
		Editors.close(con);
	}

	private static File dir() {
		var dir = new File(App.workspaceFolder(), "connections");
		if (!dir.exists()) {
			try {
				Files.createDirectories(dir.toPath());
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(Connections.class);
				log.error("failed to create connection folder: " + dir, e);
			}
		}
		return dir;
	}

}
