package app.editors.flow;

import java.util.Objects;

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
import org.openlca.ilcd.util.Flows;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.Editors;
import app.editors.RefSelectionDialog;
import app.rcp.Icon;
import app.store.RefDeps;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;

class FlowPropertySection {

	private final FlowEditor editor;
	private final Flow flow;
	private TableViewer table;

	/**
	 * We may give hints for adding additional material properties when a flow
	 * property is added. If the user follows these hints, we also have to
	 * update the material property table.
	 */
	MaterialPropertySection materialPropertySection;

	FlowPropertySection(FlowEditor editor) {
		this.editor = editor;
		this.flow = editor.flow;
	}

	void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, M.FlowProperties);
		section.setToolTipText(Tooltips.Flow_FlowProperties);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp,
			M.FlowProperty, M.ConversionFactor, M.Unit);
		table.setLabelProvider(new Label());
		table.setInput(Flows.getFlowProperties(flow));
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);

		// set tooltip texts
		var swtTable = table.getTable();
		swtTable.setToolTipText(Tooltips.Flow_FlowProperties);
		for (var col : swtTable.getColumns()) {
			col.setToolTipText(Tooltips.Flow_FlowProperties);
		}

		// add actions
		bindActions(section);
		var modifier = new ModifySupport<FlowPropertyRef>(table);
		modifier.onDouble(M.ConversionFactor, FlowPropertyRef::getMeanValue,
			(propRef, value) -> {
				propRef.withMeanValue(value);
				editor.setDirty();
			});
		Tables.onDoubleClick(table, e -> {
			FlowPropertyRef ref = Viewers.getFirstSelected(table);
			if (ref != null) {
				Editors.open(ref.getFlowProperty());
			}
		});
	}

	private void bindActions(Section section) {
		var add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		var rem = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		var ref = Actions.create(M.SetAsReference,
			Icon.des(DataSetType.FLOW_PROPERTY), this::setRef);
		Actions.bind(section, add, rem);
		Actions.bind(table, ref, add, rem);
	}

	private void add() {
		var ref = RefSelectionDialog.select(DataSetType.FLOW_PROPERTY);
		if (ref == null)
			return;
		var propRef = new FlowPropertyRef();
		var existingIDs = Flows.getFlowProperties(flow)
			.stream()
			.map(FlowPropertyRef::getDataSetInternalID)
			.toList();
		propRef.withDataSetInternalID(0);
		while (existingIDs.contains(propRef.getDataSetInternalID())) {
			propRef.withDataSetInternalID(propRef.getDataSetInternalID() + 1);
		}
		propRef.withFlowProperty(ref);
		propRef.withMeanValue(1.0);
		flow.withFlowProperties().add(propRef);
		if (Flows.getFlowProperties(flow).size() == 1) {
			// set it as reference flow property if it is the only one
			var qRef = Flows.withQuantitativeReference(flow);
			qRef.withReferenceFlowProperty(propRef.getDataSetInternalID());
		}
		table.setInput(Flows.getFlowProperties(flow));
		if (materialPropertySection != null
			&& PropertyDepsDialog.checkToAdd(editor)) {
			table.setInput(Flows.getFlowProperties(flow));
			materialPropertySection.refresh();
		}
		editor.setDirty();
	}

	private void remove() {
		FlowPropertyRef propRef = Viewers.getFirstSelected(table);
		if (propRef == null)
			return;
		var list = flow.withFlowProperties();
		list.remove(propRef);
		var qRef = Flows.getQuantitativeReference(flow);
		if (qRef != null && Objects.equals(
			qRef.getReferenceFlowProperty(), propRef.getDataSetInternalID())) {
			qRef.withReferenceFlowProperty(null);
		}
		table.setInput(Flows.getFlowProperties(flow));
		editor.setDirty();
	}

	private void setRef() {
		FlowPropertyRef propRef = Viewers.getFirstSelected(table);
		if (propRef == null)
			return;
		var qRef = Flows.withQuantitativeReference(flow);
		qRef.withReferenceFlowProperty(propRef.getDataSetInternalID());
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
			if (!(obj instanceof FlowPropertyRef ref))
				return null;
			return switch (col) {
				case 0 -> {
					Ref propRef = ref.getFlowProperty();
					yield propRef != null
							? App.s(propRef.getName())
							: null;
				}
				case 1 -> Double.toString(ref.getMeanValue());
				case 2 -> RefDeps.getRefUnit(ref.getFlowProperty());
				default -> null;
			};
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof FlowPropertyRef ref))
				return null;
			var qRef = Flows.getQuantitativeReference(flow);
			if (qRef == null)
				return null;
			return Objects.equals(
				ref.getDataSetInternalID(), qRef.getReferenceFlowProperty())
				? UI.boldFont()
				: null;
		}
	}
}
