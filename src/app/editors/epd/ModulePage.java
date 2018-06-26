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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import app.App;
import app.M;
import app.rcp.Icon;
import app.store.EpdProfiles;
import app.util.Actions;
import app.util.Controls;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ComboBoxCellModifier;
import app.util.tables.ModifySupport;
import app.util.tables.TextCellModifier;
import epd.model.EpdDataSet;
import epd.model.EpdProfile;
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
			if (Objects.equals(m1, m2))
				return Strings.compare(e1.scenario, e2.scenario);
			return m1.index - m2.index;
		});
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		toolkit = managedForm.getToolkit();
		ScrolledForm form = UI.formHeader(managedForm,
				M.EnvironmentalIndicators);
		Composite body = UI.formBody(form, managedForm.getToolkit());
		createProfileSection(body);
		createScenarioSection(body);
		moduleTable = createModuleSection(body);
		moduleTable.setInput(modules);
		resultTable = createResultSection(body);
		resultTable.refresh();
		form.reflow(true);
	}

	private void createProfileSection(Composite body) {
		Composite comp = UI.formSection(body, toolkit, "#EPD Profile");
		Combo combo = UI.formCombo(comp, toolkit, "#EPD Profile");
		int selected = -1;
		List<EpdProfile> profiles = EpdProfiles.getAll();
		profiles.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
		String[] items = new String[profiles.size()];
		for (int i = 0; i < profiles.size(); i++) {
			EpdProfile p = profiles.get(i);
			items[i] = p.name != null ? p.name : "?";
			if (Objects.equals(p.id, dataSet.profile)) {
				selected = i;
			}
		}
		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, e -> {
			int i = combo.getSelectionIndex();
			EpdProfile p = profiles.get(i);
			if (p != null) {
				dataSet.profile = p.id;
				editor.setDirty();
			}
		});
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
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		String[] columns = new String[] { M.Module, M.Scenario,
				M.ProductSystem, M.Description };
		TableViewer table = Tables.createViewer(comp, columns);
		table.setLabelProvider(new ModuleLabel());
		Tables.addSorter(table, 0, (ModuleEntry e) -> e.module.name);
		Tables.addSorter(table, 1, (ModuleEntry e) -> e.scenario);
		Tables.addSorter(table, 3, (ModuleEntry e) -> e.description);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		Action[] actions = createModuleActions();
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		ModifySupport<ModuleEntry> modifiers = new ModifySupport<>(table);
		modifiers.bind(M.Module, new ModuleModifier());
		modifiers.bind(M.Scenario, new ScenarioModifier());
		modifiers.bind(M.Description, new DescriptionModifier());
		return table;
	}

	private Action[] createModuleActions() {
		Action[] actions = new Action[2];
		actions[0] = Actions.create(M.Add, Icon.ADD.des(),
				() -> createModule());
		actions[1] = Actions.create(M.Remove, Icon.DELETE.des(),
				() -> removeModule());
		return actions;
	}

	private void createModule() {
		ModuleEntry module = new ModuleEntry();
		module.module = nextModule();
		modules.add(module);
		moduleTable.setInput(modules);
		editor.setDirty();
	}

	private Module nextModule() {
		Module[] mods = modules();
		if (mods.length == 0)
			return null;
		int selected = 0;
		for (ModuleEntry e : modules) {
			for (int i = 0; i < mods.length; i++) {
				if (!Objects.equals(e.module, mods[i]))
					continue;
				if (i >= selected) {
					selected = i + 1;
				}
				break;
			}
		}
		if (selected < mods.length) {
			return mods[selected];
		}
		return mods[0];
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
			MsgBox.error(M.ExportFailed, message);
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

	private Module[] modules() {
		List<Module> modules = EpdProfiles.modules();
		modules.sort((m1, m2) -> m1.index - m2.index);
		return modules.toArray(new Module[modules.size()]);
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
				return module != null ? module.name : null;
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
			return modules();
		}

		@Override
		protected String getText(Module module) {
			if (module == null)
				return "";
			return module.name;
		}

		@Override
		protected void setItem(ModuleEntry entry, Module module) {
			if (Objects.equals(entry.module, module))
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