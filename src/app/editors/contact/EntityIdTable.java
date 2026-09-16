package app.editors.contact;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.EpdEntityId;
import org.openlca.ilcd.util.Contacts;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

class EntityIdTable {

	private final ContactEditor editor;
	private final TableViewer table;
	private final Contact contact;

	private EntityIdTable(ContactEditor editor, Composite parent, FormToolkit tk) {
		this.editor = editor;
		this.contact = editor.contact;
		UI.formLabel(parent, tk, M.EntityIds, Tooltips.Contact_EntityIds);
		table = Tables.createViewer(parent, M.Type, M.Value);
		UI.stretchXY(table.getControl()).heightHint = 100;
		table.setLabelProvider(new LabelProvider());
		Tables.bindColumnWidths(table, 0.4, 0.6);

		var ms = new ModifySupport<EpdEntityId>(table);
		ms.bind(M.Type, new TypeModifier());
		ms.bind(M.Value, new ValueModifier());

		var add = Actions.create(M.Add, Icon.ADD.des(), this::onCreate);
		var rem = Actions.create(M.Remove, Icon.DELETE.des(), this::onRemove);
		Actions.bind(table, add, rem);
		setInput();
	}

	static void create(ContactEditor editor, Composite parent, FormToolkit tk) {
		new EntityIdTable(editor, parent, tk);
	}

	private void setInput() {
		table.setInput(Contacts.getEpdEntityIds(contact));
	}

	protected void onCreate() {
		var id = new EpdEntityId()
			.withType("Type of ID")
			.withValue("Value of ID");
		Contacts.withEpdEntityIds(contact).add(id);
		setInput();
		editor.setDirty();
	}

	protected void onRemove() {
		List<EpdEntityId> selection = Viewers.getAllSelected(table);
		for (var id : selection) {
			Contacts.withEpdEntityIds(contact).remove(id);
		}
		setInput();
		editor.setDirty();
	}

	private static class LabelProvider extends BaseLabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdEntityId id))
				return null;
			return switch (col) {
				case 0 -> id.getType();
				case 1 -> id.getValue();
				default -> null;
			};
		}
	}

	private class TypeModifier extends TextModifier<EpdEntityId> {

		@Override
		protected String getText(EpdEntityId id) {
			return id.getType();
		}

		@Override
		protected void setText(EpdEntityId id, String newText) {
			if (id == null)
				return;
			if (Objects.equals(id.getType(), newText))
				return;
			id.withType(newText);
			editor.setDirty();
		}
	}

	private class ValueModifier extends TextModifier<EpdEntityId> {

		@Override
		protected String getText(EpdEntityId id) {
			return id.getValue();
		}

		@Override
		protected void setText(EpdEntityId id, String newText) {
			if (id == null)
				return;
			if (Objects.equals(id.getValue(), newText))
				return;
			id.withValue(newText);
			editor.setDirty();
		}
	}
}
