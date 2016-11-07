package app.editors.epd;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import app.M;
import app.rcp.Icon;
import app.util.Tables;
import app.util.UI;
import app.util.tables.AbstractTableViewer;
import app.util.tables.CheckBoxCellModifier;
import app.util.tables.TextCellModifier;
import epd.model.EpdDataSet;
import epd.model.Scenario;

class ScenarioTable extends AbstractTableViewer<Scenario> {

	private final static String NAME = M.Name;
	private final static String GROUP = M.Group;
	private final static String DESCRIPTION = M.Description;
	private final static String DEFAULT = M.Default;

	private final EpdEditor editor;
	private final EpdDataSet dataSet;
	private final List<Scenario> scenarios;

	public ScenarioTable(EpdEditor editor, Composite parent) {
		super(parent);
		this.editor = editor;
		this.dataSet = editor.getDataSet();
		this.scenarios = dataSet.scenarios;
		Tables.bindColumnWidths(getViewer(), 0.25, 0.25, 0.25, 0.25);
		applyCellModifySupport();
		getViewer().refresh(true);
		UI.gridData(getViewer().getControl(), true, true).heightHint = 150;
	}

	private void applyCellModifySupport() {
		getModifySupport().bind(NAME, new TextModifier(NAME));
		getModifySupport().bind(GROUP, new TextModifier(GROUP));
		getModifySupport().bind(DESCRIPTION, new TextModifier(DESCRIPTION));
		getModifySupport().bind(DEFAULT, new DefaultModifier());
	}

	@Override
	protected String[] getColumnHeaders() {
		return new String[] { NAME, GROUP, DESCRIPTION, DEFAULT };
	}

	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new LabelProvider();
	}

	private class LabelProvider extends BaseLabelProvider implements
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
			switch (col) {
			case 0:
				return scenario.name;
			case 1:
				return scenario.group;
			case 2:
				return scenario.description;
			default:
				return null;
			}
		}
	}

	public void setInput() {
		if (scenarios == null)
			setInput(new Scenario[0]);
		else
			setInput(scenarios.toArray(new Scenario[scenarios.size()]));
		getViewer().refresh(true);
	}

	@OnAdd
	protected void onCreate() {
		Scenario scenario = new Scenario();
		scenario.name = "New scenario";
		scenario.defaultScenario = false;
		scenarios.add(scenario);
		setInput();
		editor.setDirty(true);
	}

	@OnRemove
	protected void onRemove() {
		for (Scenario scenario : getAllSelected())
			scenarios.remove(scenario);
		setInput();
		editor.setDirty(true);
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
			editor.setDirty(true);
		}
	}

	private class DefaultModifier extends CheckBoxCellModifier<Scenario> {

		@Override
		protected boolean isChecked(Scenario element) {
			return element.defaultScenario;
		}

		@Override
		protected void setChecked(Scenario element, boolean value) {
			for (Scenario scenario : dataSet.scenarios)
				if (scenario == element)
					element.defaultScenario = value;
				else
					scenario.defaultScenario = false;
			editor.setDirty(true);
		}
	}

}