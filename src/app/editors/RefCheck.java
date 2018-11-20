package app.editors;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;

import app.App;
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
			boolean b = MsgBox.ask("#Update references?",
					"The data set has outdated references to other data sets."
							+ " Do you want to update these?");
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
