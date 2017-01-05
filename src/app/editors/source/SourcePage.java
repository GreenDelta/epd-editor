package app.editors.source;

import java.util.List;

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

import app.App;
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
		super(editor, "SourcePage", "#Source Data Set");
		this.editor = editor;
		source = editor.source;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform,
				"#Source: " + App.s(source.getName()));
		tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		TextBuilder tb = new TextBuilder(editor, this, tk);
		infoSection(body, tb);
		categorySection(body);
		contacts(body);
		new FileTable(editor).render(body, tk);
		adminSection(body);
	}

	private void infoSection(Composite body, TextBuilder tb) {
		Composite comp = UI.formSection(body, tk, "#Contact information");
		DataSetInfo info = source.sourceInfo.dataSetInfo;
		tb.text(comp, "#Short name", info.name);
		tb.text(comp, "#Citation", info.citation, t -> info.citation = t);
		tb.text(comp, "#Comment", info.description);
		// TODO: source type combo
		UI.formLabel(comp, tk, "#Logo");
		RefLink logo = new RefLink(comp, tk, DataSetType.SOURCE);
		logo.setRef(info.logo);
		logo.onChange(ref -> {
			info.logo = ref;
			editor.setDirty();
		});
	}

	private void categorySection(Composite body) {
		DataSetInfo info = source.sourceInfo.dataSetInfo;
		CategorySection section = new CategorySection(editor,
				DataSetType.SOURCE, info.classifications);
		section.render(body, tk);
	}

	private void contacts(Composite body) {
		List<Ref> contacts = source.sourceInfo.dataSetInfo.contacts;
		RefTable.create(DataSetType.CONTACT, contacts)
				.withEditor(editor)
				.withTitle("#Belongs to")
				.render(body, tk);
	}

	private void adminSection(Composite body) {
		Composite comp = UI.formSection(body, tk,
				"#Administrative information");
		AdminInfo info = source.adminInfo;
		Text timeT = UI.formText(comp, tk, "#Last change");
		timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		Text uuidT = UI.formText(comp, tk, "#UUID");
		if (source.getUUID() != null)
			uuidT.setText(source.getUUID());
		VersionField vf = new VersionField(comp, tk);
		vf.setVersion(source.getVersion());
		vf.onChange(v -> info.publication.version = v);
		editor.onSaved(() -> {
			vf.setVersion(info.publication.version);
			timeT.setText(Xml.toString(info.dataEntry.timeStamp));
		});
	}

}
