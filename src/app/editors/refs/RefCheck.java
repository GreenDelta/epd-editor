package app.editors.refs;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.M;
import app.editors.Editors;
import app.store.Data;
import app.util.MsgBox;
import epd.refs.RefSync;

public class RefCheck {

	public static void on(IDataSet ds) {
		App.runInUI("Check references ...", () -> {
			if (ds == null)
				return;
			if (!RefSync.hasOutdatedRefs(ds, App.index()))
				return;
			boolean b = MsgBox.ask(M.UpdateReferences + "?",
					M.UpdateDataSetRefs_Question);
			if (!b)
				return;
			updateAndReopen(ds);
		});
	}

	public static void updateAndReopen(IDataSet ds) {
		RefSync.updateRefs(ds, App.index());
		Data.updateVersion(ds);
		RefSync.updateSelfRefVersion(ds);
		var res = Data.save(ds);
		if (res.isError()) {
			// the reopened editor would ask the same question again
			MsgBox.error(M.FailedToSaveDataSet, res.error());
			return;
		}
		Ref ref = Ref.of(ds);
		Editors.close(ref);
		Editors.open(ref);
	}
}
