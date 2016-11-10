package app.editors;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.rcp.Labels;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

public class RefTable {

	private final DataSetType type;
	private final String lang;
	private final List<Ref> refs;

	private EpdEditor editor;
	private String title = "?";

	private RefTable(DataSetType type, List<Ref> refs) {
		this.type = type;
		this.refs = refs;
		this.lang = App.lang;
	}

	public static RefTable create(DataSetType type, List<Ref> refs) {
		return new RefTable(type, refs);
	}

	public RefTable withEditor(EpdEditor editor) {
		this.editor = editor;
		return this;
	}

	public RefTable withTitle(String title) {
		this.title = title;
		return this;
	}

	public void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, title);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, Labels.get(type));
		table.setLabelProvider(new Label());
		Action[] actions = createActions(table);
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		table.setInput(refs);
		table.getTable().getColumn(0).pack();
	}

	private Action[] createActions(TableViewer table) {
		Action[] actions = new Action[2];
		actions[0] = Actions.create("#Add", Icon.ADD.des(), () -> {
			Ref ref = RefSelectionDialog.select(type);
			if (ref == null || refs.contains(ref))
				return;
			refs.add(ref);
			table.setInput(refs);
			if (editor != null)
				editor.setDirty();
		});
		actions[1] = Actions.create("#Remove", Icon.DELETE.des(), () -> {
			Ref ref = Viewers.getFirstSelected(table);
			if (ref == null)
				return;
			refs.remove(ref);
			table.setInput(refs);
			if (editor != null)
				editor.setDirty();
		});
		return actions;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Ref))
				return null;
			return Icon.img(type);
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
