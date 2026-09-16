package app.editors.epd.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;
import org.openlca.ilcd.epd.EpdProfileModule;
import org.openlca.ilcd.epd.EpdProfiles;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdModuleEntry;
import org.openlca.ilcd.util.Epds;

import app.M;
import app.Tooltips;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ComboModifier;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

/**
 * The section with the declared module entries of an EPD. Changes in this
 * section are not synchronized directly with the result section; this is done
 * via the synchronization function of the result section.
 */
class ModulesSection {

	private final EpdEditor editor;
	private final Process epd;
	private final List<EpdModuleEntry> modules;
	private final TableViewer table;

	ModulesSection(EpdEditor editor, Composite body, FormToolkit tk) {
		this.editor = editor;
		this.epd = editor.epd;
		this.modules = EpdModuleEntries.withAllOf(epd);

		var section = UI.section(body, tk, M.Modules);
		section.setToolTipText(Tooltips.EPD_Modules);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var columns = new String[]{
				M.Module,
				M.Scenario,
				M.Description};

		table = Tables.createViewer(comp, columns);
		table.setLabelProvider(new ModuleLabel());
		table.getTable().setToolTipText(Tooltips.EPD_Modules);
		Tables.addSorter(table, 0, EpdModuleEntry::getModule);
		Tables.addSorter(table, 1, EpdModuleEntry::getScenario);
		Tables.addSorter(table, 2, EpdModuleEntry::getDescription);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.5);

		var actions = createActions();
		Actions.bind(section, actions);
		Actions.bind(table, actions);
		var modifiers = new ModifySupport<EpdModuleEntry>(table);
		modifiers.bind(M.Module, new ModuleModifier());
		modifiers.bind(M.Scenario, new ScenarioModifier());
		modifiers.bind(M.Description, new DescriptionModifier());
		table.setInput(modules);
	}

	/**
	 * Returns the live list of the module entries of the EPD.
	 */
	List<EpdModuleEntry> modules() {
		return modules;
	}

	/**
	 * Refreshes the displayed texts of the module entries, e.g. when the
	 * names of the scenarios have changed.
	 */
	void refresh() {
		table.refresh();
	}

	private Action[] createActions() {
		var actions = new Action[2];
		actions[0] = Actions.create(
				M.Add, Icon.ADD.des(), this::createEntry);
		actions[1] = Actions.create(
				M.Remove, Icon.DELETE.des(), this::removeEntry);
		return actions;
	}

	private void createEntry() {
		var e = new EpdModuleEntry().withModule(nextModule());
		modules.add(e);
		table.setInput(modules);
		editor.setDirty();
	}

	private void removeEntry() {
		EpdModuleEntry e = Viewers.getFirstSelected(table);
		if (e == null)
			return;
		modules.remove(e);
		table.setInput(modules);
		editor.setDirty();
	}

	private String nextModule() {
		var mods = profileModules();
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

	private EpdProfileModule[] profileModules() {
		var profile = EpdProfiles.of(epd);
		// profiles are shared instances; we copy the module list here
		// because we should not change the order in the profile
		var mods = new ArrayList<>(profile.getModules());
		mods.sort(Comparator.comparingInt(EpdProfileModule::getIndex));
		return mods.toArray(new EpdProfileModule[0]);
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
				case 2 -> e.getDescription();
				default -> null;
			};
		}

	}

	private class ModuleModifier extends
			ComboModifier<EpdModuleEntry, String> {

		@Override
		protected String getItem(EpdModuleEntry e) {
			return e.getModule();
		}

		@Override
		protected String[] getItems(EpdModuleEntry e) {
			return Arrays.stream(profileModules())
					.map(EpdProfileModule::getName)
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
			ComboModifier<EpdModuleEntry, String> {

		@Override
		protected String getItem(EpdModuleEntry e) {
			var scenario = e.getScenario();
			return scenario != null ? scenario : "";
		}

		@Override
		protected String[] getItems(EpdModuleEntry e) {
			// the first item is an empty entry that means that no
			// scenario is assigned to the module
			var names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			for (var scenario : Epds.getScenarios(epd)) {
				var name = scenario.getName();
				if (!Strings.isBlank(name)) {
					names.add(name);
				}
			}
			var items = new ArrayList<String>();
			items.add("");
			items.addAll(names);
			return items.toArray(new String[0]);
		}

		@Override
		protected String getText(String scenario) {
			return scenario != null ? scenario : "";
		}

		@Override
		protected void setItem(EpdModuleEntry e, String scenario) {
			var value = Strings.isBlank(scenario) ? null : scenario;
			if (Objects.equals(e.getScenario(), value))
				return;
			e.withScenario(value);
			editor.setDirty();
		}
	}

	private class DescriptionModifier extends TextModifier<EpdModuleEntry> {

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
