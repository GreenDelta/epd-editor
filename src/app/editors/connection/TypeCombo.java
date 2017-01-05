package app.editors.connection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;

import app.rcp.Icon;
import app.rcp.Labels;
import app.util.Controls;

class TypeCombo {

	DataSetType selectedType = DataSetType.PROCESS;

	static TypeCombo create(Composite parent, FormToolkit tk) {
		TypeCombo combo = new TypeCombo();
		combo.render(parent, tk);
		return combo;
	}

	private void render(Composite parent, FormToolkit tk) {
		Button button = tk.createButton(parent, "", SWT.NONE);
		button.setImage(Icon.EPD.img());
		Menu menu = new Menu(button);
		DataSetType[] types = {
				DataSetType.PROCESS,
				DataSetType.CONTACT,
				DataSetType.SOURCE,
				DataSetType.FLOW,
				DataSetType.FLOW_PROPERTY,
				DataSetType.UNIT_GROUP };
		for (DataSetType type : types) {
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setText(Labels.get(type));
			item.setImage(Icon.img(type));
			Controls.onSelect(item, e -> {
				selectedType = type;
				button.setImage(Icon.img(type));
				button.setToolTipText(Labels.get(selectedType));
			});
		}
		button.setMenu(menu);
		Controls.onSelect(button, e -> menu.setVisible(true));
	}

}
