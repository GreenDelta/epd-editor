package app.editors.connection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.util.UI;

class DataSection {

	private final SodaConnection con;

	DataSection(SodaConnection con) {
		this.con = con;
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "#Data");
		UI.gridData(section, true, true);
		Composite parent = UI.sectionClient(section, tk);
		Composite comp = tk.createComposite(parent);
		UI.innerGrid(comp, 3);
		TypeCombo typeCombo = TypeCombo.create(comp, tk);
		Text searchText = tk.createText(comp, "", SWT.BORDER);
		UI.gridData(searchText, false, false).widthHint = 350;
		Button searchButton = tk.createButton(parent, M.Search, SWT.NONE);

	}

}
