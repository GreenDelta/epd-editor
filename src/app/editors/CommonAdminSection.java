package app.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.util.DataSets;

import app.M;
import app.Tooltips;
import app.util.UI;
import epd.model.Xml;

public record CommonAdminSection(BaseEditor editor, IDataSet ds) {

	public static CommonAdminSection of(BaseEditor editor, IDataSet ds) {
		return new CommonAdminSection(editor, ds);
	}

	public void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.AdministrativeInformation);
		var timeT = UI.formText(comp, tk, M.LastUpdate, Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(DataSets.getTimeStamp(ds)));

		var uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		var uuid = DataSets.getUUID(ds);
		if (uuid != null) {
			uuidT.setText(uuid);
		}

		var vf = new VersionField(comp, tk);
		vf.setVersion(DataSets.getVersion(ds));
		vf.onChange(v -> {
			DataSets.withVersion(ds, v);
			editor.setDirty();
		});

		editor.onSaved(() -> {
			vf.setVersion(DataSets.getVersion(ds));
			timeT.setText(Xml.toString(DataSets.getTimeStamp(ds)));
		});
	}

}
