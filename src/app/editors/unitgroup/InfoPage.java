package app.editors.unitgroup;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
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
import app.editors.CommonAdminSection;
import app.util.LangText;
import app.util.UI;

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
		CommonAdminSection.of(editor, unitGroup).render(body, tk);
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
}
