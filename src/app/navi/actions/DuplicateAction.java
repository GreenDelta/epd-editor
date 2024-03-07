package app.navi.actions;

import app.App;
import app.M;
import app.editors.Editors;
import app.navi.RefElement;
import app.rcp.Icon;
import app.store.Data;
import app.util.MsgBox;
import app.util.UI;
import epd.model.Xml;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DuplicateAction extends Action {

	private final RefElement e;

	public DuplicateAction(RefElement e) {
		this.e = e;
		setText(M.Duplicate);
		setImageDescriptor(Icon.SAVE_AS.des());
	}

	@Override
	public void run() {
		if (e == null || e.ref == null || !e.ref.isValid())
			return;
		var d = new InputDialog(UI.shell(), M.SaveAs,
			M.SaveAs_Message + ":", App.s(e.ref.getName()), null);
		if (d.open() != Window.OK)
			return;
		var name = d.getValue();
		try {
			var ds = duplicate(name);
			if (ds == null) {
				MsgBox.error("Could not duplicate data set type="
					+ e.ref.getType() + " id=" + e.ref.getUUID());
				return;
			}
			Data.save(ds);
			Editors.open(Ref.of(ds));
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to duplicate data set " + e.ref, ex);
			MsgBox.error("Could not duplicate data set type="
				+ e.ref.getType() + " id=" + e.ref.getUUID());
		}
	}

	private IDataSet duplicate(String name) {
		var dsClass = e.ref.getType().getDataSetClass();
		var ds = App.store().get(dsClass, e.ref.getUUID());
		if (ds == null)
			return null;
		DataSets.withUUID(ds, UUID.randomUUID().toString());
		DataSets.withVersion(ds, "00.00.000");
		DataSets.withTimeStamp(ds, Xml.now());
		DataSets.withBaseName(ds, LangString.of(name, App.lang()));
		return ds;
	}
}
