package app.editors;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.M;
import app.store.Data;
import app.util.MsgBox;
import epd.index.RefSync;

public class RefCheck {

	public static void on(IDataSet ds) {
		App.runInUI("Check references ...", () -> {
			if (ds == null)
				return;
			if (!RefSync.hasOutdatedRefs(ds, App.index))
				return;
			boolean b = MsgBox.ask(M.UpdateReferences + "?",
					M.UpdateDataSetRefs_Question);
			if (!b)
				return;
			RefSync.updateRefs(ds, App.index);
			Data.updateVersion(ds);
			Data.save(ds);
			Ref ref = Ref.of(ds);
			Editors.close(ref);
			Editors.open(ref);
		});
	}

}
