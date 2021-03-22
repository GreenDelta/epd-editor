package app.editors.epd;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.Tooltips;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.CheckBoxCellModifier;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.Scenario;

class ScenarioTable {

	private final static String NAME = M.Name;
	private final static String GROUP = M.Group;
	private final static String DESCRIPTION = M.Description;
	private final static String DEFAULT = M.Default;

	private final EpdEditor editor;
	private final TableViewer table;
	private final List<Scenario> scenarios;

	public ScenarioTable(EpdEditor editor, Section section, FormToolkit tk) {
		this.editor = editor;
		this.scenarios = editor.dataSet.scenarios;
		Composite comp = UI.sectionClient(section, tk);
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
		ModifySupport<Scenario> ms = new ModifySupport<>(table);
		ms.bind(NAME, new TextModifier(NAME));
		ms.bind(GROUP, new TextModifier(GROUP));
		ms.bind(DESCRIPTION, new TextModifier(DESCRIPTION));
		ms.bind(DEFAULT, new DefaultModifier());
	}

	private void bindActions(Section section) {
		Action add = Actions.create(M.Add, Icon.ADD.des(), this::onCreate);
		Action rem = Actions.create(M.Remove, Icon.DELETE.des(),
				this::onRemove);
		Actions.bind(section, add, rem);
		Actions.bind(table, add, rem);
	}

	public void setInput() {
		if (scenarios == null)
			table.setInput(Collections.emptyList());
		else
			table.setInput(scenarios);
	}

	protected void onCreate() {
		Scenario scenario = new Scenario();
		scenario.name = "New scenario";
		scenario.defaultScenario = false;
		scenarios.add(scenario);
		setInput();
		editor.setDirty();
	}

	protected void onRemove() {
		List<Scenario> selection = Viewers.getAllSelected(table);
		for (Scenario s : selection)
			scenarios.remove(s);
		setInput();
		editor.setDirty();
	}

	private static class LabelProvider extends BaseLabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			Scenario scenario = (Scenario) obj;
			if (col != 3)
				return null;
			if (scenario.defaultScenario)
				return Icon.CHECK_TRUE.img();
			return Icon.CHECK_FALSE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			Scenario scenario = (Scenario) obj;
			if (scenario == null)
				return "";
			return switch (col) {
			case 0 -> scenario.name;
			case 1 -> scenario.group;
			case 2 -> scenario.description;
			default -> null;
			};
		}
	}

	private class TextModifier extends TextCellModifier<Scenario> {

		private final String type;

		public TextModifier(String type) {
			this.type = type;
		}

		@Override
		protected String getText(Scenario scenario) {
			if (NAME.equals(type))
				return scenario.name;
			else if (GROUP.equals(type))
				return scenario.group;
			else if (DESCRIPTION.equals(type))
				return scenario.description;
			else
				return "";
		}

		@Override
		protected void setText(Scenario scenario, String newText) {
			if (scenario == null)
				return;
			String oldText = getText(scenario);
			if (Objects.equals(oldText, newText))
				return;
			if (NAME.equals(type))
				scenario.name = newText;
			else if (GROUP.equals(type))
				scenario.group = newText;
			else if (DESCRIPTION.equals(type))
				scenario.description = newText;
			editor.setDirty();
		}
	}

	private class DefaultModifier extends CheckBoxCellModifier<Scenario> {

		@Override
		protected boolean isChecked(Scenario element) {
			return element.defaultScenario;
		}

		@Override
		protected void setChecked(Scenario checked, boolean value) {
			if (checked == null)
				return;
			var group = checked.group;
			for (var scenario : scenarios) {
				if (Objects.equals(scenario, checked)) {
					scenario.defaultScenario = value;
				} else if (Objects.equals(group, scenario.group)) {
					scenario.defaultScenario = false;
				}
			}
			table.refresh();
			editor.setDirty();
		}
	}

}
