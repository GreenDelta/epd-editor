package app.editors.epd.results;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdScenario;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.CheckModifier;
import app.util.tables.ModifySupport;

class ScenarioTable {

	private final static String NAME = M.Name;
	private final static String GROUP = M.Group;
	private final static String DESCRIPTION = M.Description;
	private final static String DEFAULT = M.Default;

	private final EpdEditor editor;
	private final TableViewer table;
	private final Process epd;

	private Runnable onChanged;

	public ScenarioTable(EpdEditor editor, Section section, FormToolkit tk) {
		this.editor = editor;
		this.epd = editor.epd;
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, NAME, GROUP, DESCRIPTION, DEFAULT);
		table.setLabelProvider(new LabelProvider());
		table.getTable().setToolTipText(Tooltips.EPD_Scenarios);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		addModifiers();
		UI.gridData(table.getControl(), true, true).heightHint = 150;
		bindActions(section);
	}

	private void addModifiers() {
		var ms = new ModifySupport<EpdScenario>(table);
		ms.bind(NAME, new TextModifier(NAME));
		ms.bind(GROUP, new TextModifier(GROUP));
		ms.bind(DESCRIPTION, new TextModifier(DESCRIPTION));
		ms.bind(DEFAULT, new DefaultModifier());
	}

	private void bindActions(Section section) {
		var add = Actions.create(M.Add, Icon.ADD.des(), this::onCreate);
		var rem = Actions.create(M.Remove, Icon.DELETE.des(), this::onRemove);
		Actions.bind(section, add, rem);
		Actions.bind(table, add, rem);
	}

	public void onChanged(Runnable onChanged) {
		this.onChanged = onChanged;
	}

	public void setInput() {
		table.setInput(Epds.getScenarios(epd));
	}

	protected void onCreate() {
		var scenario = new EpdScenario()
			.withName("New scenario")
			.withDefaultScenario(false);
		Epds.withScenarios(epd).add(scenario);
		setInput();
		editor.setDirty();
		if (onChanged != null)
			onChanged.run();
	}

	protected void onRemove() {
		List<EpdScenario> selection = Viewers.getAllSelected(table);
		for (var s : selection) {
			Epds.withScenarios(epd).remove(s);
			ScenarioDataSection.removeDataFor(epd, s.getName());
		}
		setInput();
		editor.setDirty();
		if (onChanged != null)
			onChanged.run();
	}

	private static class LabelProvider extends BaseLabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof EpdScenario s) || col != 3)
				return null;
			return s.isDefaultScenario()
				? Icon.CHECK_TRUE.img()
				: Icon.CHECK_FALSE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdScenario s))
				return null;
			return switch (col) {
				case 0 -> s.getName();
				case 1 -> s.getGroup();
				case 2 -> App.s(s.getDescription());
				default -> null;
			};
		}
	}

	private class TextModifier extends app.util.tables.TextModifier<EpdScenario> {

		private final String field;

		public TextModifier(String field) {
			this.field = field;
		}

		@Override
		protected String getText(EpdScenario s) {
			if (NAME.equals(field))
				return s.getName();
			else if (GROUP.equals(field))
				return s.getGroup();
			else if (DESCRIPTION.equals(field))
				return App.s(s.getDescription());
			else
				return "";
		}

		@Override
		protected void setText(EpdScenario s, String newText) {
			if (s == null)
				return;
			String oldText = getText(s);
			if (Objects.equals(oldText, newText))
				return;
			if (NAME.equals(field)) {
				ScenarioDataSection.renameDataFor(epd, oldText, newText);
				s.withName(newText);
			} else if (GROUP.equals(field)) {
				s.withGroup(newText);
			} else if (DESCRIPTION.equals(field)) {
				LangString.set(s.withDescription(), newText, App.lang());
			}
			editor.setDirty();
			if (onChanged != null)
				onChanged.run();
		}
	}

	private class DefaultModifier extends CheckModifier<EpdScenario> {

		@Override
		protected boolean isChecked(EpdScenario s) {
			return s.isDefaultScenario();
		}

		@Override
		protected void setChecked(EpdScenario s, boolean value) {
			if (s == null)
				return;
			s.withDefaultScenario(value);

			if (value) {
				var group = s.getGroup();
				for (var other : Epds.getScenarios(epd)) {
					if (Objects.equals(s, other) || !other.isDefaultScenario())
						continue;
					if (Objects.equals(group, other.getGroup())) {
						other.withDefaultScenario(false);
					}
				}
			}
			table.refresh();
			editor.setDirty();
		}
	}
}
