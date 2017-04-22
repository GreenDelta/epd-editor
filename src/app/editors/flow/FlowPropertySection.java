package app.editors.flow;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.openlca.ilcd.util.Flows;

import app.App;
import app.M;
import app.editors.Editors;
import app.editors.IEditor;
import app.editors.RefSelectionDialog;
import app.rcp.Icon;
import app.store.RefUnits;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;

class FlowPropertySection {

	private final IEditor editor;
	private final Flow flow;
	private TableViewer table;

	FlowPropertySection(IEditor editor, Flow flow) {
		this.editor = editor;
		this.flow = flow;
	}

	void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, "#Flow properties");
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp,
				"#Flow property",
				"#Conversion factor",
				M.Unit);
		table.setLabelProvider(new Label());
		table.setInput(flow.flowProperties);
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		bindActions(section);
		ModifySupport<FlowPropertyRef> modifier = new ModifySupport<>(table);
		modifier.onDouble("#Conversion factor", propRef -> propRef.meanValue,
				(propRef, value) -> {
					propRef.meanValue = value;
					editor.setDirty();
				});
		Tables.onDoubleClick(table, e -> {
			FlowPropertyRef ref = Viewers.getFirstSelected(table);
			if (ref != null)
				Editors.open(ref.flowProperty);
		});
	}

	private void bindActions(Section section) {
		Action add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Action rem = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Action ref = Actions.create("#Set as reference",
				Icon.des(DataSetType.FLOW_PROPERTY), this::setRef);
		Actions.bind(section, add, rem);
		Actions.bind(table, ref, add, rem);
	}

	private void add() {
		Ref ref = RefSelectionDialog.select(DataSetType.FLOW_PROPERTY);
		if (ref == null)
			return;
		FlowPropertyRef propRef = new FlowPropertyRef();
		List<Integer> existingIDs = flow.flowProperties.stream()
				.map(pr -> pr.dataSetInternalID)
				.collect(Collectors.toList());
		propRef.dataSetInternalID = 0;
		while (existingIDs.contains(propRef.dataSetInternalID)) {
			propRef.dataSetInternalID = propRef.dataSetInternalID + 1;
		}
		propRef.flowProperty = ref;
		propRef.meanValue = 1.0;
		flow.flowProperties.add(propRef);
		table.setInput(flow.flowProperties);
		editor.setDirty();
	}

	private void remove() {
		FlowPropertyRef propRef = Viewers.getFirstSelected(table);
		if (propRef == null)
			return;
		flow.flowProperties.remove(propRef);
		QuantitativeReference qRef = Flows.getQuantitativeReference(flow);
		if (qRef != null) {
			if (propRef.dataSetInternalID == qRef.referenceFlowProperty)
				qRef.referenceFlowProperty = null;
		}
		table.setInput(flow.flowProperties);
		editor.setDirty();
	}

	private void setRef() {
		FlowPropertyRef propRef = Viewers.getFirstSelected(table);
		if (propRef == null)
			return;
		QuantitativeReference qRef = Flows.quantitativeReference(flow);
		qRef.referenceFlowProperty = propRef.dataSetInternalID;
		table.refresh();
		editor.setDirty();
	}

	private class Label extends LabelProvider implements ITableLabelProvider,
			ITableFontProvider {

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

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof FlowPropertyRef))
				return null;
			QuantitativeReference qRef = Flows.getQuantitativeReference(flow);
			if (qRef == null)
				return null;
			FlowPropertyRef ref = (FlowPropertyRef) obj;
			if (ref.dataSetInternalID == qRef.referenceFlowProperty)
				return UI.boldFont();
			return null;
		}
	}
}