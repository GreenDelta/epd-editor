package app.editors.unitgroup;

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
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroups;

import app.App;
import app.M;
import app.Tooltips;
import app.editors.IEditor;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;

class UnitSection {

	private final IEditor editor;
	private final UnitGroup group;
	private TableViewer table;

	UnitSection(IEditor editor, UnitGroup group) {
		this.editor = editor;
		this.group = group;
	}

	void render(Composite parent, FormToolkit tk) {
		Section section = UI.section(parent, tk, M.Units);
		section.setToolTipText(Tooltips.UnitGroup_Units);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		table = Tables.createViewer(composite, M.Unit, M.ConversionFactor,
				M.Comment);
		table.setLabelProvider(new Label());
		table.setInput(UnitGroups.getUnits(group));
		Tables.bindColumnWidths(table, 0.2, 0.3, 0.5);
		bindModifiers();
		bindActions(section);
	}

	private void bindModifiers() {
		ModifySupport<Unit> modifier = new ModifySupport<>(table);
		modifier.bind(M.Unit, u -> u.name, (u, name) -> {
			u.name = name;
			editor.setDirty();
		});
		modifier.onDouble(M.ConversionFactor, u -> u.factor, (u, factor) -> {
			u.factor = factor;
			editor.setDirty();
		});
		modifier.bind(M.Comment, u -> App.s(u.comment), (u, comment) -> {
			LangString.set(u.comment, comment, App.lang());
			editor.setDirty();
		});
	}

	private void bindActions(Section section) {
		Action add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Action rem = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Action ref = Actions.create(M.SetAsReference,
				Icon.des(DataSetType.FLOW_PROPERTY), this::setRef);
		Actions.bind(section, add, rem);
		Actions.bind(table, ref, add, rem);
	}

	private void add() {
		List<Unit> units = UnitGroups.units(group);
		List<Integer> existingIDs = units.stream().map(u -> u.id)
				.collect(Collectors.toList());
		Unit u = new Unit();
		u.id = 0;
		while (existingIDs.contains(u.id)) {
			u.id++;
		}
		u.name = u.id == 0 ? "unit" : "unit " + u.id;
		units.add(u);
		table.setInput(units);
		editor.setDirty();
	}

	private void remove() {
		Unit u = Viewers.getFirstSelected(table);
		if (u == null)
			return;
		List<Unit> units = UnitGroups.units(group);
		units.remove(u);
		table.setInput(units);
		editor.setDirty();
	}

	private void setRef() {
		Unit u = Viewers.getFirstSelected(table);
		if (u == null)
			return;
		QuantitativeReference qRef = UnitGroups.quantitativeReference(group);
		qRef.referenceUnit = u.id;
		table.refresh();
		editor.setDirty();
	}

	private class Label extends LabelProvider implements
			ITableLabelProvider, ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Unit))
				return null;
			Unit unit = (Unit) obj;
			switch (col) {
			case 0:
				return unit.name;
			case 1:
				return String.valueOf(unit.factor);
			case 2:
				return App.s(unit.comment);
			default:
				return null;
			}
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof Unit))
				return null;
			QuantitativeReference qRef = UnitGroups
					.getQuantitativeReference(group);
			if (qRef == null)
				return null;
			Unit u = (Unit) obj;
			if (u.id == qRef.referenceUnit)
				return UI.boldFont();
			return null;
		}
	}

}
