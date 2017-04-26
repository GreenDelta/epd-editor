package app.editors.source;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Sources;

import app.App;
import app.M;
import app.editors.CategorySection;
import app.editors.RefLink;
import app.editors.RefTable;
import app.editors.VersionField;
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
				+ App.s(source.getName());
		ScrolledForm form = UI.formHeader(mform, title.get());
		editor.onSaved(() -> form.setText(title.get()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		contacts(body);
		new FileTable(editor).render(body, tk);
		adminSection(body);
		form.reflow(true);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.infoSection(source, body, tk);
		DataSetInfo info = Sources.dataSetInfo(source);
		tb.text(comp, M.ShortName, info.name);
		tb.text(comp, M.Citation, info.citation, t -> info.citation = t);
		tb.text(comp, M.Description, info.description);
		// TODO: source type combo
		UI.formLabel(comp, tk, M.Logo);
		RefLink logo = new RefLink(comp, tk, DataSetType.SOURCE);
		logo.setRef(info.logo);
		logo.onChange(ref -> {
			info.logo = ref;
			editor.setDirty();
		});
		UI.fileLink(source, comp, tk);
	}

	private void categorySection(Composite body) {
		DataSetInfo info = Sources.dataSetInfo(source);
		CategorySection section = new CategorySection(editor,
				DataSetType.SOURCE, info.classifications);
		section.render(body, tk);
	}

	private void contacts(Composite body) {
		List<Ref> contacts = Sources.dataSetInfo(source).contacts;
		RefTable.create(DataSetType.CONTACT, contacts)
				.withEditor(editor)
				.withTitle("#Belongs to")
				.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk, M.AdministrativeInformation);
		AdminInfo info = source.adminInfo;
		Text timeT = UI.formText(comp, tk, M.LastUpdate);
		timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		Text uuidT = UI.formText(comp, tk, M.UUID);
		if (source.getUUID() != null)
			uuidT.setText(source.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(source.getVersion());
		vf.onChange(v -> {
			info.publication.version = v;
			editor.setDirty();
		});
		editor.onSaved(() -> {
			vf.setVersion(info.publication.version);
			timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		});
	}

}
