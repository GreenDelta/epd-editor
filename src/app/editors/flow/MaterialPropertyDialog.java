package app.editors.flow;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import app.M;
import app.util.Controls;
import app.util.UI;
import app.util.Viewers;
import epd.model.MaterialProperty;

class MaterialPropertyDialog extends Dialog {

	private final List<MaterialProperty> properties;
	MaterialProperty selectedProperty;

	/**
	 * Creates a dialog for selecting one of the given material properties.
	 */
	MaterialPropertyDialog(List<MaterialProperty> properties) {
		super(UI.shell());
		this.properties = properties != null ? properties : List.of();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(M.AddAMaterialProperty);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		UI.gridLayout(composite, 1);
		UI.stretchX(composite);
		new Label(composite, SWT.NONE)
			.setText(M.SelectAMaterialProperty);
		createViewer(composite);
		return parent;
	}

	private void createViewer(Composite parent) {
		var combo = new ComboViewer(parent, SWT.READ_ONLY);
		UI.stretchX(combo.getCombo());
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setLabelProvider(new PropertyLabel());
		setInput(combo);
		Controls.onSelect(combo,
			e -> selectedProperty = Viewers.getFirst(e.getSelection()));
	}

	private void setInput(ComboViewer combo) {
		combo.setInput(properties);
		if (!properties.isEmpty()) {
			selectedProperty = properties.getFirst();
			combo.setSelection(new StructuredSelection(selectedProperty));
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 200);
	}

	private static class PropertyLabel extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (!(element instanceof MaterialProperty property))
				return null;
			return property.name + " (" + property.unit + ")";
		}
	}

}
