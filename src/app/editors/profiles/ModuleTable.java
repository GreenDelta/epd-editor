package app.editors.profiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileModule;

import app.M;
import app.rcp.Icon;
import app.util.Actions;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

class ModuleTable {

	private static final String INDEX = M.Index;
	private static final String NAME = M.Name;
	private static final String DESCRIPTION = M.Description;

	private final ProfileEditor editor;
	private final List<EpdProfileModule> modules;
	private TableViewer table;

	private ModuleTable(ProfileEditor editor, EpdProfile profile) {
		this.editor = editor;
		// for built-in profiles we only work on a copy; the module list of a
		// profile is shared and must not be modified here
		this.modules = editor.isReadOnly()
			? new ArrayList<>(profile.getModules())
			: profile.withModules();
	}

	static ModuleTable of(ProfileEditor editor, EpdProfile profile) {
		return new ModuleTable(editor, profile);
	}

	void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, M.Modules);
		UI.stretchXY(section);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, INDEX, NAME, DESCRIPTION);
		Tables.bindColumnWidths(table, 0.2, 0.3, 0.5);
		table.setLabelProvider(new Label());
		if (!editor.isReadOnly()) {
			addModifiers();
			bindActions(section);
		}
		refresh();
	}

	private void addModifiers() {
		var ms = new ModifySupport<EpdProfileModule>(table);
		ms.bind(INDEX, new TextModifier<>() {
			@Override
			protected String getText(EpdProfileModule m) {
				return Integer.toString(m.getIndex());
			}

			@Override
			protected void setText(EpdProfileModule m, String text) {
				int index;
				try {
					index = Integer.parseInt(text != null ? text.trim() : "");
				} catch (NumberFormatException e) {
					MsgBox.error("Invalid number format",
						"Invalid number format: " + text);
					return;
				}
				if (index == m.getIndex())
					return;
				m.withIndex(index);
				editor.setDirty();
			}
		});

		ms.bind(NAME, new TextModifier<>() {
			@Override
			protected String getText(EpdProfileModule m) {
				return m.getName();
			}

			@Override
			protected void setText(EpdProfileModule m, String text) {
				if (Objects.equals(m.getName(), text))
					return;
				m.withName(text);
				editor.setDirty();
			}
		});

		ms.bind(DESCRIPTION, new TextModifier<>() {
			@Override
			protected String getText(EpdProfileModule m) {
				return m.getDescription();
			}

			@Override
			protected void setText(EpdProfileModule m, String text) {
				if (Objects.equals(m.getDescription(), text))
					return;
				m.withDescription(text);
				editor.setDirty();
			}
		});
	}

	private void bindActions(Section section) {
		var add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		var remove = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Actions.bind(section, add, remove);
		Actions.bind(table, add, remove);
	}

	private void add() {
		int next = modules.stream()
			.mapToInt(EpdProfileModule::getIndex)
			.max()
			.orElse(0) + 1;
		modules.add(new EpdProfileModule()
			.withIndex(next)
			.withName(M.NewModule));
		refresh();
		editor.setDirty();
	}

	private void remove() {
		List<EpdProfileModule> selected = Viewers.getAllSelected(table);
		if (selected.isEmpty())
			return;
		modules.removeAll(selected);
		refresh();
		editor.setDirty();
	}

	private void refresh() {
		modules.sort(Comparator.comparingInt(EpdProfileModule::getIndex));
		table.setInput(modules);
	}

	private static class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdProfileModule module))
				return null;
			return switch (col) {
				case 0 -> Integer.toString(module.getIndex());
				case 1 -> module.getName();
				case 2 -> module.getDescription();
				default -> null;
			};
		}
	}
}
