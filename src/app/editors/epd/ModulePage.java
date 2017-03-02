package app.editors.epd;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Actions;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ComboBoxCellModifier;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.EpdDataSet;
import epd.model.Module;
import epd.model.ModuleEntry;
import epd.model.Scenario;
import epd.util.Strings;

class ModulePage extends FormPage {

	private EpdEditor editor;
	private FormToolkit toolkit;

	private List<ModuleEntry> modules;
	private EpdDataSet dataSet;
	private ScenarioTable scenarioTable;
	private TableViewer moduleTable;
	private ModuleResultTable resultTable;

	public ModulePage(EpdEditor editor) {
		super(editor, "ModulesPage", M.EnvironmentalIndicators);
		this.editor = editor;
		dataSet = editor.dataSet;
		modules = dataSet.moduleEntries;
		Collections.sort(modules, (e1, e2) -> {
			Module m1 = e1.module;
			Module m2 = e2.module;
			if (m1 == null || m2 == null || m1 == m2)
				return Strings.compare(e1.scenario, e2.scenario);
			return Strings.compare(m1.getLabel(), m2.getLabel());
		});
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		toolkit = managedForm.getToolkit();
		ScrolledForm form = UI.formHeader(managedForm,
				M.EnvironmentalIndicators);
		Composite body = UI.formBody(form, managedForm.getToolkit());
		createScenarioSection(body);
		moduleTable = createModuleSection(body);
		moduleTable.setInput(modules);
		resultTable = createResultSection(body);
		resultTable.refresh();
		form.reflow(true);
	}

	private void createScenarioSection(Composite parent) {
		Section section = UI.section(parent, toolkit, M.Scenarios);
		section.setExpanded(false);
		UI.gridData(section, true, false);
		scenarioTable = new ScenarioTable(editor, section, toolkit);
		scenarioTable.setInput();
	}

	private TableViewer createModuleSection(Composite parent) {
		Section section = UI.section(parent, toolkit, M.Modules);
		Composite composite = UI.sectionClient(section, toolkit);
		UI.gridLayout(composite, 1);
		String[] columns = new String[] { M.Module, M.Scenario,
				M.ProductSystem, M.Description };
		TableViewer viewer = Tables.createViewer(composite, columns);
		ModuleLabel label = new ModuleLabel();
		viewer.setLabelProvider(label);
		// Viewers.sortByLabels(viewer, label, 0, 1, 2, 3);
		Tables.bindColumnWidths(viewer, 0.25, 0.25, 0.25, 0.25);
		Action[] actions = createModuleActions();
		Actions.bind(section, actions);
		Actions.bind(viewer, actions);
		ModifySupport<ModuleEntry> modifiers = new ModifySupport<>(viewer);
		modifiers.bind(M.Module, new ModuleModifier());
		modifiers.bind(M.Scenario, new ScenarioModifier());
		modifiers.bind(M.Description, new DescriptionModifier());
		return viewer;
	}

	private Action[] createModuleActions() {
		Action[] actions = new Action[2];
		actions[0] = Actions.create("#Add", Icon.ADD.des(),
				() -> createModule());
		actions[1] = Actions.create("#Remove", Icon.DELETE.des(),
				() -> removeModule());
		return actions;
	}

	private void createModule() {
		ModuleEntry module = new ModuleEntry();
		module.module = Module.A1;
		modules.add(module);
		moduleTable.setInput(modules);
		editor.setDirty();
	}

	private void removeModule() {
		ModuleEntry module = Viewers.getFirstSelected(moduleTable);
		if (module == null)
			return;
		modules.remove(module);
		moduleTable.setInput(modules);
		editor.setDirty();
	}

	private ModuleResultTable createResultSection(Composite body) {
		Section section = UI.section(body, toolkit, M.Results);
		UI.gridData(section, true, true);
		Composite composite = UI.sectionClient(section, toolkit);
		UI.gridLayout(composite, 1);
		ModuleResultTable table = new ModuleResultTable(editor, dataSet);
		table.create(composite);
		Actions.bind(section, createResultActions());
		return table;
	}

	private Action[] createResultActions() {
		Action[] actions = new Action[3];
		actions[0] = Actions.create(M.SynchronizeWithModules,
				Icon.CHECK_TRUE.des(), () -> {
					new ModuleResultSync(dataSet).run();
					resultTable.refresh();
					editor.setDirty();
				});
		actions[1] = Actions.create(M.Export, Icon.EXPORT.des(),
				() -> exportResults());
		actions[2] = Actions.create(M.Import, Icon.IMPORT.des(),
				() -> importResults());
		return actions;
	}

	private void exportResults() {
		File file = FileChooser.save("results.xlsx", "*.xlsx");
		if (file == null)
			return;
		ModuleResultExport export = new ModuleResultExport(dataSet, file);
		App.run(M.Export, export, () -> {
			if (export.isDoneWithSuccess())
				return;
			String message = "#Export failed. Is the file already opened?";
			MsgBox.error("#Export failed", message);
		});
	}

	private void importResults() {
		File file = FileChooser.open("*.xlsx");
		if (file == null)
			return;
		ModuleResultImport resultImport = new ModuleResultImport(dataSet, file);
		App.run(M.Import, resultImport, () -> {
			resultTable.refresh();
			moduleTable.refresh();
			scenarioTable.setInput();
			editor.setDirty();
		});
	}

	private class ModuleLabel extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof ModuleEntry))
				return null;
			ModuleEntry entry = (ModuleEntry) element;
			Module module = entry.module;
			switch (col) {
			case 0:
				return module != null ? module.getLabel() : null;
			case 1:
				return entry.scenario;
			case 2:
				return "";
			case 3:
				return entry.description;
			default:
				return null;
			}
		}

	}

	private class ModuleModifier extends
			ComboBoxCellModifier<ModuleEntry, Module> {

		@Override
		protected Module getItem(ModuleEntry module) {
			return module.module;
		}

		@Override
		protected Module[] getItems(ModuleEntry element) {
			return Module.values();
		}

		@Override
		protected String getText(Module type) {
			if (type == null)
				return "";
			return type.getLabel();
		}

		@Override
		protected void setItem(ModuleEntry entry, Module module) {
			if (entry.module == module)
				return;
			entry.module = module;
			editor.setDirty();
		}
	}

	private class ScenarioModifier extends
			ComboBoxCellModifier<ModuleEntry, String> {

		@Override
		protected String getItem(ModuleEntry module) {
			return module.scenario;
		}

		@Override
		protected String[] getItems(ModuleEntry element) {
			List<Scenario> scenarios = dataSet.scenarios;
			String[] names = new String[scenarios.size()];
			for (int i = 0; i < scenarios.size(); i++)
				names[i] = scenarios.get(i).name;
			Arrays.sort(names);
			return names;
		}

		@Override
		protected String getText(String scenario) {
			return scenario;
		}

		@Override
		protected void setItem(ModuleEntry module, String scenario) {
			if (Objects.equals(module.scenario, scenario))
				return;
			module.scenario = scenario;
			editor.setDirty();
		}
	}

	private class DescriptionModifier extends TextCellModifier<ModuleEntry> {

		@Override
		protected String getText(ModuleEntry module) {
			return module.description;
		}

		@Override
		protected void setText(ModuleEntry module, String text) {
			if (Objects.equals(module.description, text))
				return;
			module.description = text;
			editor.setDirty();
		}
	}

}