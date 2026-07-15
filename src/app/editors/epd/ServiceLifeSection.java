package app.editors.epd;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdServiceLife;
import org.openlca.ilcd.util.Epds;

import app.util.DoubleText;
import app.util.LangText;
import app.util.UI;

class ServiceLifeSection {

	private enum Type {

		// TODO: externalize titles M.ReferenceServiceLife etc.
		REFERENCE("Reference service life"),

		ESTIMATED("Estimated service life");

		private final String title;

		Type(String title) {
			this.title = title;
		}
	}

	private final EpdEditor editor;
	private final Process epd;
	private final Type type;

	private ServiceLifeSection(EpdEditor editor, Type type) {
		this.editor = editor;
		this.epd = editor.epd;
		this.type = type;
	}

	static void reference(EpdEditor editor, Composite body, FormToolkit tk) {
		new ServiceLifeSection(editor, Type.REFERENCE).render(body, tk);
	}

	static void estimated(EpdEditor editor, Composite body, FormToolkit tk) {
		new ServiceLifeSection(editor, Type.ESTIMATED).render(body, tk);
	}

	private EpdServiceLife withObject() {
		return type == Type.REFERENCE
			? Epds.withReferenceServiceLife(epd)
			: Epds.withEstimatedServiceLife(epd);
	}

	private EpdServiceLife getObject() {
		return type == Type.REFERENCE
			? Epds.getReferenceServiceLife(epd)
			: Epds.getEstimatedServiceLife(epd);
	}

	private void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, type.title);
		UI.gridLayout(comp, 1);
		var obj = getObject();

		var top = UI.formComposite(comp, tk);
		UI.gridData(top, true, false);

		// TODO: externalize labels, like M.NumberOfYears etc.

		DoubleText.on(editor, top, tk)
			.withLabel("Number of years")
			.withInitial(obj != null && obj.getYears() > 0 ? obj.getYears() : null)
			.onChange(ys -> withObject().withYears(ys == null || ys <= 0 ? 0 : ys))
			.render();

		LangText.builder(editor, tk)
			.nextMulti("Comment")
			.val(obj != null ? obj.getComments() : null)
			.edit(() -> withObject().withComments())
			.draw(top);
	}
}
