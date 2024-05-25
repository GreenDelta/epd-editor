package app.editors.source;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Sources;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.RefTable;
import app.editors.VersionField;
import app.util.LangText;
import app.util.TextBuilder;
import app.util.UI;
import epd.model.Xml;

class SourcePage extends FormPage {

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
		adminSection(body);
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

		// TODO: source type combo
		UI.formLabel(comp, tk, M.Logo, Tooltips.Source_Logo);
		RefLink logo = new RefLink(comp, tk, DataSetType.SOURCE);
		logo.setRef(info.getLogo());
		logo.onChange(ref -> {
			info.withLogo(ref);
			editor.setDirty();
		});
		UI.fileLink(source, comp, tk);
	}

	private void categorySection(Composite body) {
		var info = Sources.withDataSetInfo(source);
		var section = new CategorySection(editor,
				DataSetType.SOURCE, info.withClassifications());
		section.render(body, tk);
	}

	private void contacts(Composite body) {
		List<Ref> contacts = Sources.withDataSetInfo(source)
				.withContacts();
		RefTable.create(DataSetType.CONTACT, contacts)
				.withEditor(editor)
				.withTitle("Belongs to")
				.withTooltip(Tooltips.Source_BelongsTo)
				.render(body, tk);
	}

	private void adminSection(Composite body) {
		var comp = UI.formSection(body, tk, M.AdministrativeInformation);

		Text timeT = UI.formText(comp, tk,
				M.LastUpdate, Tooltips.All_LastUpdate);
		timeT.setText(Xml.toString(Sources.getTimeStamp(source)));

		Text uuidT = UI.formText(comp, tk, M.UUID, Tooltips.All_UUID);
		var uuid = Sources.getUUID(source);
		if (uuid != null) {
			uuidT.setText(uuid);
		}

		var vf = new VersionField(comp, tk);
		vf.setVersion(Sources.getVersion(source));
		vf.onChange(v -> {
			Sources.withVersion(source, v);
			editor.setDirty();
		});

		editor.onSaved(() -> {
			vf.setVersion(Sources.getVersion(source));
			timeT.setText(Xml.toString(Sources.getTimeStamp(source)));
		});
	}

}
