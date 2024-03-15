package app.editors.epd;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
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
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.Tooltips;
import app.rcp.Icon;
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
import epd.profiles.EpdProfile;
import epd.profiles.EpdProfiles;
import epd.profiles.Module;
import epd.util.Strings;

class ResultPage extends FormPage {

	private final EpdEditor editor;
	private FormToolkit toolkit;

	private final List<EpdModuleEntry> modules;
	private final EpdDataSet epd;
	private ScenarioTable scenarioTable;
	private TableViewer moduleTable;
	private ResultTable resultTable;

	public ResultPage(EpdEditor editor) {
		super(editor, "ModulesPage", M.EnvironmentalIndicators);
		this.editor = editor;
		epd = editor.dataSet;
		modules = Epds.withModuleEntries(epd.process);
		modules.sort((e1, e2) -> {
			int c = Strings.compare(e1.getModule(), e2.getModule());
			return c == 0
				? Strings.compare(e1.getScenario(), e2.getScenario())
				: c;
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
		Composite comp = UI.formSection(body, toolkit,
			M.EPDProfile, Tooltips.EPD_EPDProfile);
		Combo combo = UI.formCombo(comp, toolkit,
			M.EPDProfile, Tooltips.EPD_EPDProfile);
		int selected = -1;
		List<EpdProfile> profiles = EpdProfiles.getAll();
		profiles.sort((p1, p2) -> Strings.compare(p1.getName(), p2.getName()));
		String[] items = new String[profiles.size()];
		for (int i = 0; i < profiles.size(); i++) {
			EpdProfile p = profiles.get(i);
			items[i] = p.getName() != null ? p.getName() : "?";
			if (Objects.equals(p.getId(), epd.process.getEpdProfile())) {
				selected = i;
			} else if (epd.process.getEpdProfile() == null && EpdProfiles.isDefault(p)) {
				selected = i;
				epd.process.withEpdProfile(p.getId());
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
				epd.process.withEpdProfile(p.getId());
				editor.setDirty();
			}
		});
	}

	private void createScenarioSection(Composite parent) {
		Section section = UI.section(parent, toolkit, M.Scenarios);
		section.setToolTipText(Tooltips.EPD_Scenarios);
		section.setExpanded(false);
		UI.gridData(section, true, false);
		scenarioTable = new ScenarioTable(editor, section, toolkit);
		scenarioTable.setInput();
	}

	private TableViewer createModuleSection(Composite parent) {
		Section section = UI.section(parent, toolkit, M.Modules);
		section.setToolTipText(Tooltips.EPD_Modules);
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		String[] columns = new String[]{M.Module, M.Scenario,
			M.ProductSystem, M.Description};
		TableViewer table = Tables.createViewer(comp, columns);
		table.setLabelProvider(new ModuleLabel());
		table.getTable().setToolTipText(Tooltips.EPD_Modules);
		Tables.addSorter(table, 0, EpdModuleEntry::getModule);
		Tables.addSorter(table, 1, EpdModuleEntry::getScenario);
		Tables.addSorter(table, 3, EpdModuleEntry::getDescription);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		Action[] actions = createModuleActions();
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		var modifiers = new ModifySupport<EpdModuleEntry>(table);
		modifiers.bind(M.Module, new ModuleModifier());
		modifiers.bind(M.Scenario, new ScenarioModifier());
		modifiers.bind(M.Description, new DescriptionModifier());
		return table;
	}

	private Action[] createModuleActions() {
		Action[] actions = new Action[2];
		actions[0] = Actions.create(
			M.Add, Icon.ADD.des(), this::createModuleEntry);
		actions[1] = Actions.create(
			M.Remove, Icon.DELETE.des(), this::removeModule);
		return actions;
	}

	private void createModuleEntry() {
		var e = new EpdModuleEntry()
			.withModule(nextModule());
		modules.add(e);
		moduleTable.setInput(modules);
		editor.setDirty();
	}

	private String nextModule() {
		Module[] mods = modules();
		if (mods.length == 0)
			return null;
		int selected = 0;
		for (var e : modules) {
			for (int i = 0; i < mods.length; i++) {
				if (!Objects.equals(e.getModule(), mods[i].getName()))
					continue;
				if (i >= selected) {
					selected = i + 1;
				}
				break;
			}
		}
		if (selected < mods.length) {
			return mods[selected].getName();
		}
		return mods[0].getName();
	}

	private void removeModule() {
		EpdModuleEntry e = Viewers.getFirstSelected(moduleTable);
		if (e == null)
			return;
		modules.remove(e);
		moduleTable.setInput(modules);
		editor.setDirty();
	}

	private ResultTable createResultSection(Composite body) {
		Section section = UI.section(body, toolkit, M.Results);
		section.setToolTipText(Tooltips.EPD_Results);
		UI.gridData(section, true, true);
		Composite composite = UI.sectionClient(section, toolkit);
		UI.gridLayout(composite, 1);
		var table = new ResultTable(editor, epd.process);
		table.create(composite);
		Actions.bind(section, createResultActions());
		return table;
	}

	private Action[] createResultActions() {
		Action[] actions = new Action[3];
		actions[0] = Actions.create(M.SynchronizeWithModules,
			Icon.CHECK_TRUE.des(), () -> {
				new ResultSync(epd.process).run();
				resultTable.refresh();
				editor.setDirty();
			});
		actions[1] = Actions.create(
			M.Export, Icon.EXPORT.des(), this::exportResults);
		actions[2] = Actions.create(
			M.Import, Icon.IMPORT.des(), this::importResults);
		return actions;
	}

	private void exportResults() {
		File file = FileChooser.save("results.xlsx", "*.xlsx");
		if (file == null)
			return;
		ResultExport export = new ResultExport(epd, file);
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
		ResultImport resultImport = new ResultImport(epd.process, file);
		App.run(M.Import, resultImport, () -> {
			resultTable.refresh();
			moduleTable.refresh();
			scenarioTable.setInput();
			editor.setDirty();
		});
	}

	private Module[] modules() {
		EpdProfile profile = EpdProfiles.get(epd.process);
		List<Module> modules = profile.getModules();
		modules.sort(Comparator.comparingInt(Module::getIndex));
		return modules.toArray(new Module[0]);
	}

	private static class ModuleLabel extends LabelProvider implements
		ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof EpdModuleEntry e))
				return null;
			return switch (col) {
				case 0 -> e.getModule();
				case 1 -> e.getScenario();
				case 2 -> "";
				case 3 -> e.getDescription();
				default -> null;
			};
		}

	}

	private class ModuleModifier extends
		ComboBoxCellModifier<EpdModuleEntry, String> {

		@Override
		protected String getItem(EpdModuleEntry e) {
			return e.getModule();
		}

		@Override
		protected String[] getItems(EpdModuleEntry e) {
			return Arrays.stream(modules())
				.map(Module::getName)
				.toArray(String[]::new);
		}

		@Override
		protected String getText(String s) {
			return s;
		}

		@Override
		protected void setItem(EpdModuleEntry e, String module) {
			if (Objects.equals(e.getModule(), module))
				return;
			e.withModule(module);
			editor.setDirty();
		}
	}

	private class ScenarioModifier extends
		ComboBoxCellModifier<EpdModuleEntry, String> {

		@Override
		protected String getItem(EpdModuleEntry e) {
			return e.getScenario();
		}

		@Override
		protected String[] getItems(EpdModuleEntry e) {
			var scenarios = Epds.getScenarios(epd.process);
			String[] names = new String[scenarios.size()];
			for (int i = 0; i < scenarios.size(); i++) {
				names[i] = scenarios.get(i).getName();
			}
			Arrays.sort(names);
			return names;
		}

		@Override
		protected String getText(String scenario) {
			return scenario;
		}

		@Override
		protected void setItem(EpdModuleEntry e, String scenario) {
			if (Objects.equals(e.getScenario(), scenario))
				return;
			e.withScenario(scenario);
			editor.setDirty();
		}
	}

	private class DescriptionModifier extends TextCellModifier<EpdModuleEntry> {

		@Override
		protected String getText(EpdModuleEntry e) {
			return e.getDescription();
		}

		@Override
		protected void setText(EpdModuleEntry e, String text) {
			if (Objects.equals(e.getDescription(), text))
				return;
			e.withDescription(text);
			editor.setDirty();
		}
	}

}
