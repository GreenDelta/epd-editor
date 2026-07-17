package app.editors.epd.results;

import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdEolData;
import org.openlca.ilcd.processes.epd.EpdScenario;
import org.openlca.ilcd.processes.epd.EpdUseStageData;
import org.openlca.ilcd.util.Epds;

import app.M;
import app.editors.epd.EpdEditor;
import app.util.Colors;
import app.util.Controls;
import app.util.LangText;
import app.util.UI;

class ScenarioDataSection {

	private final EpdEditor editor;
	private final FormToolkit tk;
	private final Composite detailComp;
	private final Combo combo;

	ScenarioDataSection(EpdEditor editor, Composite body, FormToolkit tk) {
		this.editor = editor;
		this.tk = tk;
		var section = UI.section(body, tk, M.QuantitativeScenarioData);
		section.setExpanded(false);
		var client = UI.sectionClient(section, tk);
		UI.gridLayout(client, 1);
		combo = UI.formCombo(client, tk, "Scenario");
		combo.addSelectionListener(Controls.onSelect(_ -> renderDetail()));
		detailComp = tk.createComposite(client);
		UI.gridData(detailComp, true, false);
		UI.gridLayout(detailComp, 1);
		fillCombo();
		if (combo.getItemCount() > 0) {
			combo.select(0);
			renderDetail();
		}
	}

	void refresh() {
		var old = combo.getText();
		fillCombo();
		if (combo.getItemCount() == 0)
			return;
		int idx = indexOf(old);
		combo.select(Math.max(idx, 0));
		renderDetail();
	}

	private int indexOf(String name) {
		if (name == null)
			return -1;
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (Objects.equals(combo.getItem(i), name))
				return i;
		}
		return -1;
	}

	private void fillCombo() {
		var scenarios = Epds.getScenarios(editor.epd);
		combo.setItems(scenarios.stream()
			.map(EpdScenario::getName)
			.filter(Objects::nonNull)
			.toArray(String[]::new));
	}

	private void renderDetail() {
		for (var child : detailComp.getChildren()) {
			child.dispose();
		}
		int idx = combo.getSelectionIndex();
		if (idx < 0)
			return;
		var scenario = combo.getItem(idx);
		if (scenario == null)
			return;
		renderUseStage(scenario);
		renderEol(scenario);
		detailComp.getParent().layout(true, true);
	}

	private void renderUseStage(String scenario) {
		var useData = useStageOf(scenario);
		LangText.builder(editor, tk)
			.nextMulti(M.ImpactsOnSoilAndWater)
			.val(useData.getSoilAndWaterImpacts())
			.edit(useData::withSoilAndWaterImpacts)
			.draw(detailComp);
	}

	private void renderEol(String scenario) {
		var eol = eolOf(scenario);

		var comp = tk.createComposite(detailComp);
		UI.gridData(comp, true, false);
		UI.innerGrid(comp, 7);

		// Row 0: Waste collection
		boldCell(comp, M.WasteCollection);
		amountCell(comp, M.SeparatelyCollected,
			eol.getCollection() != null ? eol.getCollection().getSeparate() : null,
			val -> eol.withCollection().withSeparate(val));
		amountCell(comp, M.WithMixedWaste,
			eol.getCollection() != null ? eol.getCollection().getWithMixedWaste() : null,
			val -> eol.withCollection().withWithMixedWaste(val));

		// Row 1: Resource recovery (vertical span 2, covers row 2's col 0)
		boldCell(comp, M.ResourceRecovery, 2);
		amountCell(comp, M.ForReUse,
			eol.getRecovery() != null ? eol.getRecovery().getReuse() : null,
			val -> eol.withRecovery().withReuse(val));
		amountCell(comp, M.ForRecycling,
			eol.getRecovery() != null ? eol.getRecovery().getRecycling() : null,
			val -> eol.withRecovery().withRecycling(val));

		// Row 2: For energy recovery (col 0 covered by resource recovery vspan)
		amountCell(comp, M.ForEnergyRecovery,
			eol.getRecovery() != null ? eol.getRecovery().getEnergyRecovery() : null,
			val -> eol.withRecovery().withEnergyRecovery(val));
		UI.filler(comp, tk);
		UI.filler(comp, tk);
		UI.filler(comp, tk);

		// Row 3: Waste disposal
		boldCell(comp, M.WasteDisposal);
		amountCell(comp, M.ForFinalDeposition,
			eol.getDisposal() != null ? eol.getDisposal().getFinalDeposition() : null,
			val -> eol.withDisposal().withFinalDeposition(val));
		UI.filler(comp, tk);
		UI.filler(comp, tk);
		UI.filler(comp, tk);
	}

	private void boldCell(Composite parent, String text) {
		var label = tk.createLabel(parent, text, SWT.NONE);
		label.setFont(UI.boldFont());
		var gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		label.setLayoutData(gd);
	}

	private void boldCell(Composite parent, String text, int vspan) {
		var label = tk.createLabel(parent, text, SWT.NONE);
		label.setFont(UI.boldFont());
		var gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.verticalSpan = vspan;
		label.setLayoutData(gd);
	}

	private void amountCell(Composite parent, String label,
		Double initial, Consumer<Double> onChange) {
		var subLabel = tk.createLabel(parent, label, SWT.NONE);
		var gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.verticalIndent = 2;
		subLabel.setLayoutData(gd);

		var text = tk.createText(parent, "", SWT.BORDER);
		UI.gridData(text, true, false);
		if (initial != null) {
			text.setText(Double.toString(initial));
		}
		tk.createLabel(parent, "kg");

		text.addModifyListener(_ -> {
			var s = text.getText().trim();
			if (Strings.isBlank(s)) {
				onChange.accept(null);
			} else {
				try {
					double val = Double.parseDouble(s);
					onChange.accept(val);
					text.setBackground(Colors.white());
				} catch (Exception ex) {
					text.setBackground(Colors.errorColor());
				}
			}
			editor.setDirty();
		});
	}

	private EpdUseStageData useStageOf(String scenario) {
		if (scenario == null)
			return null;
		var data = Epds.withScenarioData(editor.epd);
		for (var d : data.withUseStageData()) {
			if (Objects.equals(d.getScenario(), scenario))
				return d;
		}
		var d = new EpdUseStageData().withScenario(scenario);
		data.withUseStageData().add(d);
		return d;
	}

	private EpdEolData eolOf(String scenario) {
		if (scenario == null)
			return null;
		var data = Epds.withScenarioData(editor.epd);
		for (var d : data.withEolData()) {
			if (Objects.equals(d.getScenario(), scenario))
				return d;
		}
		var d = new EpdEolData().withScenario(scenario);
		data.withEolData().add(d);
		return d;
	}

	static void removeDataFor(Process epd, String scenario) {
		if (scenario == null)
			return;
		var data = Epds.withScenarioData(epd);
		data.withUseStageData().removeIf(d -> Objects.equals(d.getScenario(), scenario));
		data.withEolData().removeIf(d -> Objects.equals(d.getScenario(), scenario));
	}

	static void renameDataFor(Process epd, String oldName, String newName) {
		if (oldName == null || newName == null || Objects.equals(oldName, newName))
			return;
		var data = Epds.withScenarioData(epd);
		for (var d : data.withUseStageData()) {
			if (Objects.equals(d.getScenario(), oldName)) {
				d.withScenario(newName);
			}
		}
		for (var d : data.withEolData()) {
			if (Objects.equals(d.getScenario(), oldName)) {
				d.withScenario(newName);
			}
		}
	}
}
