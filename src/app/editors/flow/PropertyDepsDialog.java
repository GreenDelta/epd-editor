package app.editors.flow;

import app.M;
import app.rcp.Icon;
import app.store.MaterialProperties;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import epd.model.EpdProduct;
import epd.model.MaterialProperty;
import epd.model.MaterialPropertyValue;
import epd.util.Strings;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.Flows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * When a flow property is added to a flow, we open this dialog when we have
 * some suggestions for additional flow and material properties that the user
 * may want to add.
 */
class PropertyDepsDialog extends FormDialog {

	private final Config config;

	PropertyDepsDialog(Config config) {
		super(UI.shell());
		this.config = config;
	}

	static boolean add(EpdProduct product) {
		if (product == null || product.flow == null
			|| Flows.getType(product.flow) != FlowType.PRODUCT_FLOW)
			return false;
		var config = Config.create(product);
		if (config.selected.isEmpty())
			return false;
		var dialog = new PropertyDepsDialog(config);
		int returnCode = dialog.open();
		return returnCode == OK && !config.selected.isEmpty();
	}

	@Override
	protected void configureShell(Shell shell) {
		shell.setText("Add additional properties");
		shell.setSize(500, 500);
		UI.center(UI.shell(), shell);
		super.configureShell(shell);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		var body = UI.formBody(mform.getForm(), tk);

		UI.formLabel(body, tk,
			"You may also want to add the following properties?");
		var table = Tables.createViewer(body, M.MaterialProperties);
		Tables.bindColumnWidths(table, 1.0);
		table.setLabelProvider(new TableLabel());
		table.setInput(config.properties);

		// handle selection changes
		Tables.onClick(table, e -> {
			MaterialProperty prop = Viewers.getFirstSelected(table);
			if (prop == null)
				return;
			var selected = config.selected.contains(prop);
			if (selected) {
				config.selected.remove(prop);
			} else {
				config.selected.add(prop);
			}
			table.refresh();
		});
	}

	@Override
	protected void okPressed() {
		for (var prop : config.selected) {
			var val = new MaterialPropertyValue();
			val.property = prop;
			val.value = 1.0;
			config.product.properties.add(val);
		}
		super.okPressed();
	}

	private class TableLabel extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof MaterialProperty prop))
				return null;
			return config.selected.contains(prop)
				? Icon.CHECK_TRUE.img()
				: Icon.CHECK_FALSE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof MaterialProperty prop))
				return null;
			return prop.name;
		}
	}

	private static class Config {

		EpdProduct product;
		final List<MaterialProperty> properties = new ArrayList<>();
		final List<MaterialProperty> selected = new ArrayList<>();

		static Config create(EpdProduct product) {
			var conf = new Config();
			if (product == null || product.flow == null)
				return conf;

			conf.product = product;

			// collect non-present properties
			var usedIDs = product.properties.stream()
				.filter(val -> val.property != null)
				.map(val -> val.property.id)
				.collect(Collectors.toSet());
			for (var prop : MaterialProperties.get()) {
				if (usedIDs.contains(prop.id))
					continue;
				conf.properties.add(prop);
			}
			if (conf.properties.isEmpty())
				return conf;

			conf.properties.sort((p1, p2) -> Strings.compare(p1.name, p2.name));

			// find new property candidates
			var pairs = new String[][]{
				{"area", "layer thickness"},
				{"area", "grammage"},
				{"volume", "bulk density"},
				{"volume", "gross density"},
				{"normal volume", "bulk density"},
				{"normal volume", "gross density"},
			};
			for (var flowProp : Flows.getFlowProperties(product.flow)) {
				if (flowProp.getFlowProperty() == null)
					continue;
				var name = LangString.getVal(flowProp.getFlowProperty().getName(), "en");
				if (name == null)
					continue;
				for (var pair : pairs) {
					if (!eq(name, pair[0]))
						continue;
					for (var matProp : conf.properties) {
						if (conf.selected.contains(matProp))
							continue;
						if (eq(matProp.name, pair[1])) {
							conf.selected.add(matProp);
						}
					}
				}
			}
			return conf;
		}

		static boolean eq(String s1, String s2) {
			if (s1 == null && s2 == null)
				return true;
			if (s1 == null || s2 == null)
				return false;
			return s1.trim().equalsIgnoreCase(s2.trim());
		}
	}
}
