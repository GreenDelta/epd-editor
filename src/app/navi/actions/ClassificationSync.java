package app.navi.actions;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXB;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.lists.CategorySystem;

import app.navi.Navigator;
import app.rcp.Icon;
import app.store.CategorySystems;
import app.util.MsgBox;

public class ClassificationSync extends Action {

	private final SodaConnection con;

	public ClassificationSync(SodaConnection con) {
		this.con = con;
		this.setText(con.toString());
		this.setImageDescriptor(Icon.DOWNLOAD.des());
	}

	@Override
	public void run() {
		boolean q = MsgBox.ask("#Update classifications",
				"#Do you want to update the classifications?");
		if (!q)
			return;
		String urlSpec = con.url + "/categorySystems/OEKOBAU.DAT";
		try {
			URL url = new URL(urlSpec);
			URLConnection urlCon = url.openConnection();
			urlCon.setConnectTimeout(8000);
			InputStream in = urlCon.getInputStream();
			CategorySystem system = JAXB.unmarshal(in, CategorySystem.class);
			CategorySystems.put(system);
			Navigator.refreshFolders();
		} catch (Exception e) {
			MsgBox.error("Classification update failed", e.getMessage());
		}
	}

}
