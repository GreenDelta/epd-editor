package app.editors.settings;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;

class ProfileSecion {

	private final SettingsPage page;
	private TableViewer table;

	ProfileSecion(SettingsPage page) {
		this.page = page;
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "#Validation profiles");
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, M.Name, M.Version, M.File);
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		bindActions(section);
	}

	private void bindActions(Section section) {
		Action ref = Actions.create("#Select as active profile", Icon.OK.des(),
				this::selectActive);
		Action add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Action del = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Actions.bind(table, ref, add, del);
		Actions.bind(section, ref, add, del);
	}

	private void add() {

	}

	private void remove() {

	}

	private void selectActive() {

	}

}
