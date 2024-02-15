package app.editors.flow;

import app.M;
import app.store.MaterialProperties;
import app.util.UI;
import app.util.Viewers;
import epd.model.MaterialProperty;
import epd.util.Strings;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MaterialPropertyDialog extends Dialog {

	MaterialProperty selectedProperty;

	public MaterialPropertyDialog() {
		super(UI.shell());
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
		UI.gridData(composite, true, false);
		new Label(composite, SWT.NONE)
			.setText(M.SelectAMaterialProperty);
		createViewer(composite);
		return parent;
	}

	private void createViewer(Composite parent) {
		var combo = new ComboViewer(parent, SWT.READ_ONLY);
		UI.gridData(combo.getCombo(), true, false);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setLabelProvider(new PropertyLabel());
		setInput(combo);
		combo.addSelectionChangedListener(
			e -> selectedProperty = Viewers.getFirst(e.getSelection()));
	}

	private void setInput(ComboViewer combo) {
		try {
			var props = MaterialProperties.get();
			props.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
			combo.setInput(props);
			if (!props.isEmpty()) {
				selectedProperty = props.get(0);
				var selection = new StructuredSelection(selectedProperty);
				combo.setSelection(selection);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to set viewer input", e);
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
