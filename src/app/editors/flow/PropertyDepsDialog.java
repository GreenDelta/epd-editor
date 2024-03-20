package app.editors.flow;

import app.M;
import app.rcp.Icon;
import app.store.MaterialProperties;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
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
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.util.Flows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * When a flow property is added to a flow, we open this dialog when we have
 * some suggestions for additional material properties that the user may want
 * to add.
 */
class PropertyDepsDialog extends FormDialog {

	private final DialogData data;

	PropertyDepsDialog(DialogData data) {
		super(UI.shell());
		this.data = data;
	}

	static boolean checkToAdd(FlowEditor editor) {
		if (editor == null
			|| editor.product == null
			|| Flows.getType(editor.product) != FlowType.PRODUCT_FLOW)
			return false;
		var config = DialogData.of(editor);
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
		table.setInput(data.unusedProps);

		// handle selection changes
		Tables.onClick(table, e -> {
			MaterialProperty prop = Viewers.getFirstSelected(table);
			if (prop == null)
				return;
			var selected = data.selected.contains(prop);
			if (selected) {
				data.selected.remove(prop);
			} else {
				data.selected.add(prop);
			}
			table.refresh();
		});
	}

	@Override
	protected void okPressed() {
		for (var prop : data.selected) {
			var val = new MaterialPropertyValue();
			val.property = prop;
			val.value = 1.0;
			data.editor.materialProperties.add(val);
		}
		super.okPressed();
	}

	private class TableLabel extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof MaterialProperty prop))
				return null;
			return data.selected.contains(prop)
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

	private record DialogData(
		FlowEditor editor,
		List<MaterialProperty> unusedProps,
		Set<MaterialProperty> selected
	) {

		static DialogData of(FlowEditor editor) {
			var unused = unusedPropertiesOf(editor);
			return new DialogData(
				editor,
				unused,
				defaultSelectionOf(editor.product, unused));
		}

		/**
		 * Collects all material properties that are not used in the edited flow yet.
		 */
		private static List<MaterialProperty> unusedPropertiesOf(FlowEditor editor) {
			var usedIDs = editor.materialProperties.stream()
				.filter(val -> val.property != null)
				.map(val -> val.property.id)
				.collect(Collectors.toSet());
			var props = new ArrayList<MaterialProperty>();
			for (var prop : MaterialProperties.get()) {
				if (usedIDs.contains(prop.id))
					continue;
				props.add(prop);
			}
			props.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
			return props;
		}

		/**
		 * Selects the material properties that could be added to the flow based on
		 * the flow properties of the flow that are already present in the edited flow.
		 */
		private static Set<MaterialProperty> defaultSelectionOf(
			Flow flow, List<MaterialProperty> unusedProps
		) {

			// relations between flow properties and material properties
			var relations = new String[][]{
				{"area", "layer thickness"},
				{"area", "grammage"},
				{"volume", "bulk density"},
				{"volume", "gross density"},
				{"normal volume", "bulk density"},
				{"normal volume", "gross density"},
			};

			var selection = new HashSet<MaterialProperty>();
			for (var flowProp : Flows.getFlowProperties(flow)) {
				if (flowProp.getFlowProperty() == null)
					continue;
				var name = LangString.getVal(flowProp.getFlowProperty().getName(), "en");
				if (name == null)
					continue;
				for (var rel : relations) {
					if (!eq(name, rel[0]))
						continue;
					for (var matProp : unusedProps) {
						if (eq(matProp.name, rel[1])) {
							selection.add(matProp);
						}
					}
				}
			}

			return selection;
		}

		private static boolean eq(String s1, String s2) {
			if (s1 == null && s2 == null)
				return true;
			if (s1 == null || s2 == null)
				return false;
			return s1.trim().equalsIgnoreCase(s2.trim());
		}
	}

}
