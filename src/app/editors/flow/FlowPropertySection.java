package app.editors.flow;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.FlowPropertyRef;

import app.App;
import app.M;
import app.editors.IEditor;
import app.rcp.Icon;
import app.store.RefUnits;
import app.util.Tables;
import app.util.UI;

class FlowPropertySection {

	private final List<FlowPropertyRef> flows;

	FlowPropertySection(IEditor editor, DataSetType type,
			List<FlowPropertyRef> flows) {

		this.flows = flows;
	}

	void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, "#Flow properties");
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer viewer = Tables.createViewer(comp,
				"#Flow property",
				"#Conversion factor",
				M.Unit);
		viewer.setLabelProvider(new Label());
		viewer.setInput(flows);
		Tables.bindColumnWidths(viewer, 0.4, 0.3, 0.3);
	}

	private class Label extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			if (col == 0)
				return Icon.img(DataSetType.FLOW_PROPERTY);
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof FlowPropertyRef))
				return null;
			FlowPropertyRef ref = (FlowPropertyRef) obj;
			switch (col) {
			case 0:
				Ref propRef = ref.flowProperty;
				if (propRef == null)
					return null;
				return App.s(propRef.name);
			case 1:
				return Double.toString(ref.meanValue);
			case 2:
				return RefUnits.get(ref.flowProperty);
			default:
				return null;
			}
		}

	}

}