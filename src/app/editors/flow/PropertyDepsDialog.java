package app.editors.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.util.Flows;

import app.M;
import app.rcp.Icon;
import app.store.MaterialProperties;
import app.util.Tables;
import app.util.UI;
import epd.model.EpdProduct;
import epd.model.MaterialProperty;

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
		if (config.selectedProperties.isEmpty())
			return false;
		var dialog = new PropertyDepsDialog(config);
		int returnCode = dialog.open();
		return returnCode == OK && !config.selectedProperties.isEmpty();
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

		// file selection
		UI.formLabel(body, tk,
				"You may also want to add the following properties?");
		var table = Tables.createViewer(body, M.MaterialProperties);
		Tables.bindColumnWidths(table, 1.0);
		table.setLabelProvider(new TableLabel());
		table.setInput(config.properties);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	private class TableLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof MaterialProperty))
				return null;
			var prop = (MaterialProperty) obj;
			return config.selectedProperties.contains(prop)
					? Icon.CHECK_TRUE.img()
					: Icon.CHECK_FALSE.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof MaterialProperty))
				return null;
			var prop = (MaterialProperty) obj;
			return prop.name;
		}
	}

	private static class Config {

		EpdProduct product;
		final List<MaterialProperty> properties = new ArrayList<>();
		final List<MaterialProperty> selectedProperties = new ArrayList<>();

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

			// TODO: select properties
			conf.selectedProperties.addAll(conf.properties);
			return conf;
		}
	}

}
