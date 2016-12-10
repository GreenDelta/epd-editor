package app.editors.flow;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.M;
import app.store.Store;
import app.util.UI;
import app.util.Viewers;
import epd.model.MaterialProperty;
import epd.util.Strings;

class MaterialPropertyDialog extends Dialog {

	private MaterialProperty selectedProperty;

	public MaterialPropertyDialog(Shell shell) {
		super(shell);
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

	private ComboViewer createViewer(Composite parent) {
		ComboViewer viewer = new ComboViewer(parent, SWT.READ_ONLY);
		UI.gridData(viewer.getCombo(), true, false);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new PropertyLabel());
		setInput(viewer);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedProperty = Viewers.getFirst(event.getSelection());
			}
		});
		return viewer;
	}

	private void setInput(ComboViewer viewer) {
		try {
			List<MaterialProperty> props = Store.getMaterialProperties();
			Collections.sort(props,
					(p1, p2) -> Strings.compare(p1.name, p2.name));
			viewer.setInput(props);
			if (props.size() > 0) {
				selectedProperty = props.get(0);
				StructuredSelection selection = new StructuredSelection(
						selectedProperty);
				viewer.setSelection(selection);
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

	public MaterialProperty getSelectedProperty() {
		return selectedProperty;
	}

	private class PropertyLabel extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (!(element instanceof MaterialProperty))
				return null;
			MaterialProperty property = (MaterialProperty) element;
			return property.name + " (" + property.unit + ")";
		}
	}

}