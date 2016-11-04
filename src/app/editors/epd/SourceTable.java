package app.editors.epd;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.M;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

class SourceTable {

	private final String lang;
	private final List<Ref> sources;
	private EpdEditor editor;
	private String title = M.Sources;

	private SourceTable(List<Ref> sources, String lang) {
		this.sources = sources;
		this.lang = lang;
	}

	public static SourceTable create(List<Ref> sources, String lang) {
		return new SourceTable(sources, lang);
	}

	public SourceTable withEditor(EpdEditor editor) {
		this.editor = editor;
		return this;
	}

	public SourceTable withTitle(String title) {
		this.title = title;
		return this;
	}

	public void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, title);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, M.Source);
		table.setLabelProvider(new Label());
		Tables.bindColumnWidths(table, 1);
		Action[] actions = createSourceActions(table);
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		table.setInput(sources);
	}

	private Action[] createSourceActions(TableViewer table) {
		Action[] actions = new Action[2];
		// TODO: select a source
		actions[0] = Actions.create("#Add", Icon.ADD.des(), () -> {
			// BaseDescriptor d = ModelSelectionDialog.select(ModelType.SOURCE);
			// if (!(d instanceof SourceDescriptor))
			// return;
			// sources.add(Refs.of(d, lang));
			table.setInput(sources);
			if (editor != null)
				editor.setDirty(true);
		});
		actions[1] = Actions.create("#Remove", Icon.DELETE.des(), () -> {
			Ref ref = Viewers.getFirstSelected(table);
			if (ref == null)
				return;
			sources.remove(ref);
			table.setInput(sources);
			if (editor != null)
				editor.setDirty(true);
		});
		return actions;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Ref))
				return null;
			return Icon.SOURCE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Ref))
				return null;
			Ref ref = (Ref) obj;
			return LangString.getFirst(ref.name, lang);
		}
	}

}
