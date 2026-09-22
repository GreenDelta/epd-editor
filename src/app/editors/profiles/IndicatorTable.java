package app.editors.profiles;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.epd.EpdProfile;
import org.openlca.ilcd.epd.EpdProfileIndicator;

import app.App;
import app.M;
import app.editors.Editors;
import app.editors.refs.RefSelectionDialog;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.CheckModifier;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

class IndicatorTable {

	private static final String INDICATOR = M.Indicator;
	private static final String TYPE = M.Type;
	private static final String UNIT = M.UnitReference;
	private static final String CODE = M.Code;
	private static final String GROUP = M.Group;
	private static final String INPUT = M.InputFlow;

	private final ProfileEditor editor;
	private final EpdProfile profile;
	private List<EpdProfileIndicator> indicators;
	private TableViewer table;

	IndicatorTable(ProfileEditor editor, EpdProfile profile) {
		this.editor = editor;
		this.profile = profile;
	}

	void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, M.EnvironmentalIndicators);
		UI.stretchXY(section);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp,
			INDICATOR, TYPE, UNIT, CODE, GROUP, INPUT);
		Tables.bindColumnWidths(table, 0.32, 0.14, 0.2, 0.1, 0.14, 0.1);
		table.setLabelProvider(new Label());

		indicators = editor.isReadOnly()
			? profile.getIndicators()
			: profile.withIndicators();
		if (indicators == null) {
			indicators = List.of();
		}
		table.setInput(indicators);

		if (!editor.isReadOnly()) {
			addModifiers();
			bindActions(section);
		}

		Tables.onDoubleClick(table, _ -> {
			EpdProfileIndicator indicator = Viewers.getFirstSelected(table);
			if (indicator == null)
				return;
			var ref = indicator.getRef();
			var indexRef = App.index().find(ref);
			if (indexRef != null) {
				Editors.open(indexRef);
			}
		});
	}

	private void addModifiers() {
		var ms = new ModifySupport<EpdProfileIndicator>(table);
		ms.bind(CODE, new TextModifier<>() {
			@Override
			protected String getText(EpdProfileIndicator i) {
				return i.getCode();
			}

			@Override
			protected void setText(EpdProfileIndicator i, String text) {
				if (Objects.equals(i.getCode(), text))
					return;
				i.withCode(text);
				editor.setDirty();
			}
		});

		ms.bind(GROUP, new TextModifier<>() {
			@Override
			protected String getText(EpdProfileIndicator i) {
				return i.getGroup();
			}

			@Override
			protected void setText(EpdProfileIndicator i, String text) {
				if (Objects.equals(i.getGroup(), text))
					return;
				i.withGroup(text);
				editor.setDirty();
			}
		});

		ms.bind(INPUT, new CheckModifier<>() {
			@Override
			protected boolean isChecked(EpdProfileIndicator i) {
				return i.isInputIndicator();
			}

			@Override
			protected void setChecked(EpdProfileIndicator i, boolean value) {
				i.withInputIndicator(value);
				editor.setDirty();
			}
		});

	}

	private void bindActions(Section section) {
		var addFlow = Actions.create(
			M.AddFlowIndicator, Icon.ADD.des(), this::addFlowIndicator);
		var addImpact = Actions.create(
			M.AddLCIAIndicator, Icon.ADD.des(), this::addImpactIndicator);
		var setUnit = Actions.create(
			M.SetUnit, Icon.UNIT.des(), this::setUnit);
		var remove = Actions.create(
			M.Remove, Icon.DELETE.des(), this::remove);
		Actions.bind(section, addFlow, addImpact, setUnit, remove);
		Actions.bind(table, addFlow, addImpact, setUnit, remove);
	}

	private void addFlowIndicator() {
		var ref = RefSelectionDialog.select(DataSetType.FLOW);
		if (ref != null) {
			add(new EpdProfileIndicator().withRef(ref));
		}
	}

	private void addImpactIndicator() {
		var ref = RefSelectionDialog.select(DataSetType.IMPACT_METHOD);
		if (ref != null) {
			add(new EpdProfileIndicator().withRef(ref));
		}
	}

	private void add(EpdProfileIndicator indicator) {
		if (indicators.contains(indicator))
			return;
		indicators.add(indicator);
		table.setInput(indicators);
		editor.setDirty();
	}

	private void setUnit() {
		EpdProfileIndicator indicator = Viewers.getFirstSelected(table);
		if (indicator == null)
			return;
		var unit = RefSelectionDialog.select(DataSetType.UNIT_GROUP);
		if (unit == null)
			return;
		indicator.withUnit(unit);
		table.refresh(indicator);
		editor.setDirty();
	}

	private void remove() {
		List<EpdProfileIndicator> selected = Viewers.getAllSelected(table);
		if (selected.isEmpty())
			return;
		indicators.removeAll(selected);
		table.setInput(indicators);
		editor.setDirty();
	}

	private static class Label extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof EpdProfileIndicator indicator))
				return null;
			return switch (col) {
				case 1 -> indicator.isInventoryIndicator()
					? Icon.img(DataSetType.FLOW)
					: Icon.img(DataSetType.IMPACT_METHOD);
				case 2 -> Icon.UNIT.img();
				case 5 -> indicator.isInputIndicator()
					? Icon.CHECK_TRUE.img()
					: Icon.CHECK_FALSE.img();
				default -> null;
			};
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdProfileIndicator indicator))
				return null;
			return switch (col) {
				case 0 -> App.s(indicator.getRef());
				case 1 -> indicator.isInventoryIndicator()
					? M.Flow
					: M.LCIAMethod;
				case 2 -> App.s(indicator.getUnit());
				case 3 -> indicator.getCode();
				case 4 -> indicator.getGroup();
				default -> null;
			};
		}
	}
}
