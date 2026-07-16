package app.editors.epd.results;

import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdEolData;
import org.openlca.ilcd.processes.epd.EpdScenario;
import org.openlca.ilcd.processes.epd.EpdUseStageData;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.editors.epd.EpdEditor;
import app.util.Colors;
import app.util.Controls;
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
		combo.select(idx >= 0 ? idx : 0);
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
		var scenarioName = combo.getItem(idx);
		if (scenarioName == null)
			return;
		renderUseStage(scenarioName);
		renderEol(scenarioName);
		detailComp.getParent().layout(true, true);
	}

	private void renderUseStage(String scenarioName) {
		var useData = useStageOf(scenarioName);
		var text = UI.formMultiText(detailComp, tk,
			M.ImpactsOnSoilAndWater);
		var val = LangString.get(useData.getSoilAndWaterImpacts(), App.lang());
		if (val != null) {
			text.setText(val);
		}
		text.addModifyListener(_ -> {
			var s = text.getText();
			if (s.isBlank()) {
				useData.withSoilAndWaterImpacts(List.of());
			} else {
				LangString.set(useData.withSoilAndWaterImpacts(), s, App.lang());
			}
			editor.setDirty();
		});
	}

	private void renderEol(String scenarioName) {
		var eol = eolOf(scenarioName);

		UI.formLabel(detailComp, tk, M.WasteCollection);
		amountField(detailComp, M.SeparatelyCollectedPart,
			eol.getCollection() != null ? eol.getCollection().getSeparate() : null,
			val -> eol.withCollection().withSeparate(val));
		amountField(detailComp, M.WithMixedWaste,
			eol.getCollection() != null ? eol.getCollection().getWithMixedWaste() : null,
			val -> eol.withCollection().withWithMixedWaste(val));

		UI.formLabel(detailComp, tk, M.ResourceRecovery);
		amountField(detailComp, M.ForReUse,
			eol.getRecovery() != null ? eol.getRecovery().getReuse() : null,
			val -> eol.withRecovery().withReuse(val));
		amountField(detailComp, M.ForRecycling,
			eol.getRecovery() != null ? eol.getRecovery().getRecycling() : null,
			val -> eol.withRecovery().withRecycling(val));
		amountField(detailComp, M.ForEnergyRecovery,
			eol.getRecovery() != null ? eol.getRecovery().getEnergyRecovery() : null,
			val -> eol.withRecovery().withEnergyRecovery(val));

		UI.formLabel(detailComp, tk, M.WasteDisposal);
		amountField(detailComp, M.ForFinalDeposition,
			eol.getDisposal() != null ? eol.getDisposal().getFinalDeposition() : null,
			val -> eol.withDisposal().withFinalDeposition(val));
	}

	private void amountField(Composite parent, String label,
		Double initial, java.util.function.Consumer<Double> onChange) {
		var comp = tk.createComposite(parent);
		UI.gridData(comp, true, false);
		UI.gridLayout(comp, 3);
		UI.formLabel(comp, tk, label);
		var text = tk.createText(comp, "", SWT.BORDER);
		UI.gridData(text, true, false);
		if (initial != null) {
			text.setText(Double.toString(initial));
		}
		tk.createLabel(comp, "kg");
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

	private EpdUseStageData useStageOf(String scenarioName) {
		if (scenarioName == null)
			return null;
		var data = Epds.withScenarioData(editor.epd);
		for (var d : data.withUseStageData()) {
			if (Objects.equals(d.getScenario(), scenarioName))
				return d;
		}
		var d = new EpdUseStageData().withScenario(scenarioName);
		data.withUseStageData().add(d);
		return d;
	}

	private EpdEolData eolOf(String scenarioName) {
		if (scenarioName == null)
			return null;
		var data = Epds.withScenarioData(editor.epd);
		for (var d : data.withEolData()) {
			if (Objects.equals(d.getScenario(), scenarioName))
				return d;
		}
		var d = new EpdEolData().withScenario(scenarioName);
		data.withEolData().add(d);
		return d;
	}

	static void removeDataFor(Process epd, String scenarioName) {
		if (scenarioName == null)
			return;
		var data = Epds.withScenarioData(epd);
		data.withUseStageData().removeIf(d -> Objects.equals(d.getScenario(), scenarioName));
		data.withEolData().removeIf(d -> Objects.equals(d.getScenario(), scenarioName));
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
