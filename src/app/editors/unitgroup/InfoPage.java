package app.editors.unitgroup;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroups;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.VersionField;
import app.util.LangText;
import app.util.UI;
import epd.model.Xml;

class InfoPage extends FormPage {

	private final UnitGroup unitGroup;
	private final UnitGroupEditor editor;
	private FormToolkit tk;

	InfoPage(UnitGroupEditor editor) {
		super(editor, "UnitGroupPage", M.UnitGroup);
		this.editor = editor;
		this.unitGroup = editor.unitGroup;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.UnitGroup + ": "
				+ App.s(UnitGroups.getName(unitGroup));
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		infoSection(body);
		categorySection(body);
		new UnitSection(editor, unitGroup).render(body, tk);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body) {
		var comp = UI.infoSection(unitGroup, body, tk);
		var info = UnitGroups.withDataSetInfo(unitGroup);
		var tb = LangText.builder(editor, tk);

		tb.next(M.Name, Tooltips.UnitGroup_Name)
				.val(info.getName())
				.edit(info::withName)
				.draw(comp);

		tb.next(M.Description, Tooltips.UnitGroup_Description)
				.val(info.getComment())
				.edit(info::withComment)
				.draw(comp);

		UI.fileLink(unitGroup, comp, tk);
	}

	private void categorySection(Composite body) {
		var info = UnitGroups.withDataSetInfo(unitGroup);
		var section = new CategorySection(editor,
				DataSetType.UNIT_GROUP, info.withClassifications());
		section.render(body, tk);
	}

	private void adminSection(Composite body) {
		var comp = UI.formSection(body, tk, M.AdministrativeInformation);

		Text timeT = UI.formText(comp, tk, M.LastUpdate,
				Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(UnitGroups.getTimeStamp(unitGroup)));

		Text uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		var uuid = UnitGroups.getUUID(unitGroup);
		if (uuid != null) {
			uuidT.setText(uuid);
		}

		var vf = new VersionField(comp, tk);
		vf.setVersion(UnitGroups.getVersion(unitGroup));
		vf.onChange(v -> {
			UnitGroups.withVersion(unitGroup, v);
			editor.setDirty();
		});

		editor.onSaved(() -> {
			vf.setVersion(UnitGroups.getVersion(unitGroup));
			timeT.setText(Xml.toString(UnitGroups.getTimeStamp(unitGroup)));
		});
	}
}
