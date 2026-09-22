package app.editors.epd.results;

import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;
import org.openlca.ilcd.processes.Process;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.epd.EpdEditor;
import app.rcp.Icon;
import app.store.Profiles;
import app.util.Actions;
import app.util.Controls;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.UI;

public class ResultPage extends FormPage {

	private final EpdEditor editor;
	private final Process epd;
	private ScenarioTable scenarioTable;
	private ScenarioDataSection scenarioData;
	private ModulesSection modulesSection;
	//private ResultTable resultTable;
	private ResultMatrix resultMatrix;

	public ResultPage(EpdEditor editor) {
		super(editor, "ModulesPage", M.EnvironmentalIndicators);
		this.editor = editor;
		epd = editor.epd;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var form = UI.formHeader(mForm, M.EnvironmentalIndicators);
		var body = UI.formBody(form, mForm.getToolkit());
		createProfileSection(body, tk);
		createScenarioSection(body, tk);
		scenarioData = new ScenarioDataSection(editor, body, tk);
		modulesSection = new ModulesSection(editor, body, tk);
		resultMatrix = createResultSection(body, tk);
		form.reflow(true);
	}

	private void createProfileSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.EPDProfile, Tooltips.EPD_EPDProfile);
		var combo = UI.formCombo(comp, tk, M.EPDProfile, Tooltips.EPD_EPDProfile);
		int selected = -1;
		var profiles = Profiles.getAll();
		profiles.sort((p1, p2) -> Strings.compareIgnoreCase(p1.getName(), p2.getName()));
		var items = new String[profiles.size()];
		for (int i = 0; i < profiles.size(); i++) {
			var profile = profiles.get(i);
			items[i] = profile.getName() != null ? profile.getName() : "?";
			if (Objects.equals(profile, editor.getProfile())) {
				selected = i;
			}
		}

		combo.setItems(items);
		if (selected >= 0) {
			combo.select(selected);
		}
		Controls.onSelect(combo, _ -> {
			int i = combo.getSelectionIndex();
			editor.setProfile(profiles.get(i));
			// Note: we do not refresh the module and result sections
			// here. The declared modules do not really change with the
			// profile; it is more the indicator IDs that are affected.
			// We need to think about this later.
		});
	}

	private void createScenarioSection(Composite parent, FormToolkit tk) {
		var section = UI.section(parent, tk, M.Scenarios);
		section.setToolTipText(Tooltips.EPD_Scenarios);
		section.setExpanded(false);
		UI.stretchX(section);
		scenarioTable = new ScenarioTable(editor, section, tk);
		scenarioTable.onChanged(() -> {
			scenarioData.refresh();
			modulesSection.refresh();
		});
		scenarioTable.setInput();
	}

	private ResultMatrix createResultSection(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, M.Results);
		section.setToolTipText(Tooltips.EPD_Results);
		UI.stretchXY(section);
		var composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		Actions.bind(section, createResultActions());
		var matrix = new ResultMatrix(editor, composite);
		matrix.render(modulesSection.modules());
		return matrix;
	}

	private Action[] createResultActions() {
		var actions = new Action[3];
		actions[0] = Actions.create(M.SynchronizeWithModules,
				Icon.CHECK_TRUE.des(), () -> {
					new ResultSync(epd, editor.getProfile()).run();
					// resultTable.refresh();
					resultMatrix.render(modulesSection.modules());
					editor.setDirty();
				});
		actions[1] = Actions.create(
				M.Export, Icon.EXPORT.des(), this::exportResults);
		actions[2] = Actions.create(
				M.Import, Icon.IMPORT.des(), this::importResults);
		return actions;
	}

	private void exportResults() {
		var file = FileChooser.save("results.xlsx", "*.xlsx");
		if (file == null)
			return;
		var export = ExcelExport.of(epd, editor.getProfile(), file);
		App.run(M.Export, export, () -> {
			if (export.isDoneWithSuccess())
				return;
			String message = "Export failed. Is the file already opened?";
			MsgBox.error(M.ExportFailed, message);
		});
	}

	private void importResults() {
		var file = FileChooser.open("*.xlsx");
		if (file == null)
			return;
		var resultImport = ExcelImport.of(epd, editor.getProfile(), file);
		App.run(M.Import, resultImport, () -> {
			// resultTable.refresh();
			modulesSection.refresh();
			scenarioTable.setInput();
			scenarioData.refresh();
			resultMatrix.render(modulesSection.modules());
			editor.setDirty();
		});
	}

}
