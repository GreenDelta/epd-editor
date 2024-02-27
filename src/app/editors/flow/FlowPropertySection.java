package app.editors.flow;

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
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.util.Flows;

import java.util.Objects;

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
		this.flow = editor.product.flow;
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
		modifier.onDouble(M.ConversionFactor, propRef -> propRef.getMeanValue(),
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
		var existingIDs = flow.withFlowProperties()
			.stream()
			.map(pr -> pr.getDataSetInternalID())
			.toList();
		propRef.withDataSetInternalID(0);
		while (existingIDs.contains(propRef.getDataSetInternalID())) {
			propRef.withDataSetInternalID(propRef.getDataSetInternalID() + 1);
		}
		propRef.withFlowProperty(ref);
		propRef.withMeanValue(1.0);
		Flows.getFlowProperties(flow).add(propRef);
		if (Flows.getFlowProperties(flow).size() == 1) {
			// set it as reference flow property if it is the only one
			flow.withFlowInfo()
				.withQuantitativeReference()
				.withReferenceFlowProperty(propRef.getDataSetInternalID());
		}
		table.setInput(Flows.getFlowProperties(flow));
		if (materialPropertySection != null
			&& PropertyDepsDialog.add(editor.product)) {
			table.setInput(Flows.getFlowProperties(flow));
			materialPropertySection.refresh();
		}
		editor.setDirty();
	}

	private void remove() {
		FlowPropertyRef propRef = Viewers.getFirstSelected(table);
		if (propRef == null)
			return;
		var list = Flows.getFlowProperties(flow);
		list.remove(propRef);
		if (list.isEmpty()) {
			flow.withFlowProperties(null);
		}
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
		flow.withFlowInfo()
			.withQuantitativeReference()
			.withReferenceFlowProperty(propRef.getDataSetInternalID());
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
			switch (col) {
				case 0:
					Ref propRef = ref.getFlowProperty();
					if (propRef == null)
						return null;
					return LangString.getFirst(propRef.getName(), App.lang());
				case 1:
					return Double.toString(ref.getMeanValue());
				case 2:
					return RefDeps.getRefUnit(ref.getFlowProperty());
				default:
					return null;
			}
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof FlowPropertyRef ref))
				return null;
			var qRef = Flows.getQuantitativeReference(flow);
			if (qRef == null)
				return null;
			return Objects.equals(ref.getDataSetInternalID(), qRef.getReferenceFlowProperty())
				? UI.boldFont()
				: null;
		}
	}
}
