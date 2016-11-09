package app.navi.actions;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.navi.Navigator;
import app.navi.RefElement;
import app.rcp.Icon;
import app.util.MsgBox;

public class RefDeleteAction extends Action {

	private final RefElement e;

	public RefDeleteAction(RefElement e) {
		this.e = e;
		setText("#Delete Data Set");
		setImageDescriptor(Icon.DELETE.des());
	}

	@Override
	public void run() {
		if (e == null || e.ref == null)
			return;
		boolean b = MsgBox.ask("#Delete Data Set?",
				"#Do you really want to delete the data "
						+ "set permanently?");
		if (!b)
			return;
		Ref ref = e.ref;
		try {
			App.store.delete(ref.getDataSetClass(), ref.uuid);
			App.index.remove(ref);
			App.dumpIndex();
			// TODO: the ref can be multiple times in the tree
			// -> we have to refresh all direct parents
			Navigator.refresh(e.getParent());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete data set " + ref, e);
		}
	}

}
