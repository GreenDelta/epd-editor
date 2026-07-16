package app.editors.epd;

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Epds;

import app.M;
import app.util.Controls;
import app.util.LangText;
import app.util.UI;

class PcrComplianceSection {

	private final EpdEditor editor;
	private final Process epd;

	private PcrComplianceSection(EpdEditor editor) {
		this.editor = editor;
		this.epd = editor.epd;
	}

	static void create(EpdEditor editor, Composite body, FormToolkit tk) {
		new PcrComplianceSection(editor).render(body, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.PcrCompliance);
		var init = Epds.getPcrCompliance(epd);

		check(comp, tk, M.PcrAllocation,
			init != null && init.isAllocation(),
			b -> Epds.withPcrCompliance(epd).withAllocation(b));

		check(comp, tk, M.PcrCutOffRules,
			init != null && init.isCutOffRules(),
			b -> Epds.withPcrCompliance(epd).withCutOffRules(b));

		check(comp, tk, M.PcrUpstreamDataDeviating,
			init != null && init.isUpstreamDataDeviating(),
			b -> Epds.withPcrCompliance(epd).withUpstreamDataDeviating(b));

		LangText.builder(editor, tk)
			.nextMulti(M.Comment)
			.val(init != null ? init.getComments() : null)
			.edit(() -> Epds.withPcrCompliance(epd).withComments())
			.draw(comp);
	}

	private void check(Composite comp, FormToolkit tk,
		String label, boolean selected,
		Consumer<Boolean> fn) {
		var btn = UI.formCheckBox(comp, tk, label);
		btn.setSelection(selected);
		Controls.onSelect(btn, _ -> {
			fn.accept(btn.getSelection());
			editor.setDirty();
		});
	}
}
