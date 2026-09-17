package app.editors.source;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceType;
import org.openlca.ilcd.util.Sources;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.CommonAdminSection;
import app.editors.refs.RefLink;
import app.editors.refs.RefTableSection;
import app.rcp.Labels;
import app.util.Controls;
import app.util.LangText;
import app.util.TextBuilder;
import app.util.UI;

class SourcePage extends FormPage {

	/// The source types in the order in which they are shown in the combo box.
	private static final SourceType[] SOURCE_TYPES = {
		SourceType.ARTICLE_IN_PERIODICAL,
		SourceType.CHAPTER_IN_ANTHOLOGY,
		SourceType.DIRECT_MEASUREMENT,
		SourceType.MONOGRAPH,
		SourceType.ORAL_COMMUNICATION,
		SourceType.PERSONAL_WRITTEN_COMMUNICATION,
		SourceType.QUESTIONNAIRE,
		SourceType.SOFTWARE_OR_DATABASE,
		SourceType.OTHER_UNPUBLISHED_AND_GREY_LITERATURE,
		SourceType.UNDEFINED
	};

	private final Source source;
	private final SourceEditor editor;
	private FormToolkit tk;

	SourcePage(SourceEditor editor) {
		super(editor, "SourcePage", M.Source);
		this.editor = editor;
		source = editor.source;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Supplier<String> title = () -> M.Source + ": "
				+ App.s(Sources.getName(source));
		var form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		infoSection(body);
		categorySection(body);
		contacts(body);
		new FileTable(editor).render(body, tk);
		CommonAdminSection.of(editor, source).render(body, tk);
		form.reflow(true);
	}

	private void infoSection(Composite body) {
		var comp = UI.infoSection(source, body, tk);
		var info = Sources.withDataSetInfo(source);
		var tb = LangText.builder(editor, tk);

		tb.next(M.ShortName, Tooltips.Source_ShortName)
				.val(info.getName())
				.edit(info::withName)
				.draw(comp);

		new TextBuilder(editor, tk)
				.text(comp, M.Citation, Tooltips.Source_Citation,
						info.getCitation(), info::withCitation);

		tb.nextMulti(M.Description, Tooltips.Source_Description)
				.val(info.getDescription())
				.edit(info::withDescription)
				.draw(comp);

		createSourceTypeCombo(comp, info);

		UI.formLabel(comp, tk, M.Logo, Tooltips.Source_Logo);
		RefLink logo = new RefLink(comp, tk, DataSetType.SOURCE);
		logo.setRef(info.getLogo());
		logo.onChange(ref -> {
			info.withLogo(ref);
			editor.setDirty();
		});
		UI.fileLink(source, comp, tk);
	}

	private void createSourceTypeCombo(Composite comp, DataSetInfo info) {
		var combo = UI.formCombo(comp, tk, M.SourceType);
		var items = new String[SOURCE_TYPES.length];
		int selected = -1;
		for (int i = 0; i < SOURCE_TYPES.length; i++) {
			items[i] = Labels.get(SOURCE_TYPES[i]);
			if (SOURCE_TYPES[i] == info.getType()) {
				selected = i;
			}
		}
		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, _ -> {
			int i = combo.getSelectionIndex();
			if (i < 0 || i >= SOURCE_TYPES.length)
				return;
			info.withType(SOURCE_TYPES[i]);
			editor.setDirty();
		});
	}

	private void categorySection(Composite body) {
		var info = Sources.withDataSetInfo(source);
		var section = new CategorySection(editor,
				DataSetType.SOURCE, info.withClassifications());
		section.render(body, tk);
	}

	private void contacts(Composite body) {
		RefTableSection.create(DataSetType.CONTACT)
				.withSupplier(() -> Sources.withDataSetInfo(source).withContacts())
				.withEditor(editor)
				.withTitle("Belongs to")
				.withTooltip(Tooltips.Source_BelongsTo)
				.render(body, tk);
	}
}
