package app.navi.actions;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.descriptors.CategorySystemList;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.lists.CategorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.navi.Navigator;
import app.rcp.Icon;
import app.store.CategorySystems;
import app.util.MsgBox;

public class ClassificationSync extends Action {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final SodaConnection con;
	private final String system;

	public ClassificationSync(SodaConnection con) {
		this(con, null);
	}

	public ClassificationSync(SodaConnection con, String system) {
		this.con = con;
		this.system = system;
		this.setText(con.toString().replace("@", "::"));
		this.setImageDescriptor(Icon.DOWNLOAD.des());
	}

	@Override
	public void run() {
		boolean q = MsgBox.ask(M.UpdateClassifications,
				M.UpdateClassificationsQuestion);
		if (!q)
			return;
		try (SodaClient client = new SodaClient(con)) {
			List<String> names = getSystems(client);
			for (String name : names) {
				CategorySystem system = client.getCategorySystem(name);
				if (system != null) {
					CategorySystems.put(system);
					log.info("updated system {}", system.name);
				}
			}
			Navigator.refreshFolders();
		} catch (Exception e) {
			MsgBox.error("Classification update failed", e.getMessage());
		}
	}

	private List<String> getSystems(SodaClient client) throws Exception {
		CategorySystemList list = client.getCategorySystemList();
		if (list == null)
			return Collections.emptyList();
		List<String> all = list.getNames();
		if (system != null) {
			if (all.contains(system))
				return Collections.singletonList(system);
			log.info("{} is not a system on the server", system);
			return Collections.emptyList();
		}
		return all;
	}
}
