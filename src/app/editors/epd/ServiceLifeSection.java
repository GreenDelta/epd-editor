package app.editors.epd;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdConditionCategory;
import org.openlca.ilcd.processes.epd.EpdConditionFactor;
import org.openlca.ilcd.processes.epd.EpdServiceLife;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.editors.refs.RefTable;
import app.rcp.Icon;
import app.util.Actions;
import app.util.DoubleText;
import app.util.LangText;
import app.util.LangTextDialog;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ComboModifier;
import app.util.tables.DialogModifier;
import app.util.tables.ModifySupport;

class ServiceLifeSection {

	private enum Type {

		REFERENCE(M.ReferenceServiceLife),

		ESTIMATED(M.EstimatedServiceLife);

		final String title;

		Type(String title) {
			this.title = title;
		}
	}

	private final EpdEditor editor;
	private final Process epd;
	private final Type type;

	private ServiceLifeSection(EpdEditor editor, Type type) {
		this.editor = editor;
		this.epd = editor.epd;
		this.type = type;
	}

	static void reference(EpdEditor editor, Composite body, FormToolkit tk) {
		new ServiceLifeSection(editor, Type.REFERENCE).render(body, tk);
	}

	static void estimated(EpdEditor editor, Composite body, FormToolkit tk) {
		new ServiceLifeSection(editor, Type.ESTIMATED).render(body, tk);
	}

	private EpdServiceLife withObject() {
		return type == Type.REFERENCE
			? Epds.withReferenceServiceLife(epd)
			: Epds.withEstimatedServiceLife(epd);
	}

	private EpdServiceLife getObject() {
		return type == Type.REFERENCE
			? Epds.getReferenceServiceLife(epd)
			: Epds.getEstimatedServiceLife(epd);
	}

	private void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, type.title);
		UI.gridLayout(comp, 1);
		var obj = getObject();

		var top = UI.formComposite(comp, tk);
		UI.gridData(top, true, false);

		DoubleText.on(editor, top, tk)
			.withLabel(M.NumberOfYears)
			.withInitial(obj != null && obj.getYears() > 0 ? obj.getYears() : null)
			.onChange(ys -> withObject().withYears(ys == null || ys <= 0 ? 0 : ys))
			.render();

		LangText.builder(editor, tk)
			.nextMulti(M.Comment)
			.val(obj != null ? obj.getComments() : null)
			.edit(() -> withObject().withComments())
			.draw(top);

		UI.formLabel(top, tk, M.UsedStandards);
		RefTable.create(DataSetType.SOURCE)
			.withEditor(editor)
			.withInitial(obj == null ? null : obj.getStandards())
			.withSupplier(() -> withObject().withStandards())
			.render(top, tk);

		UI.formLabel(top, tk, M.Documentation);
		RefTable.create(DataSetType.SOURCE)
			.withEditor(editor)
			.withInitial(obj == null ? null : obj.getDocumentations())
			.withSupplier(() -> withObject().withDocumentations())
			.render(top, tk);

		var table = Tables.createViewer(comp,
			M.UseConditions,
			M.Value,
			M.ObjectSpecificGrade,
			M.ReferenceGrade,
			M.Comment);
		table.setLabelProvider(new FactorLabel());
		Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2);

		var ms = new ModifySupport<EpdConditionFactor>(table);
		ms.bind(M.UseConditions, new CategoryModifier());
		ms.onDouble(M.Value, EpdConditionFactor::getValue, (f, val) -> {
			f.withValue(val);
			editor.setDirty();
		});
		ms.bind(M.ObjectSpecificGrade, new GradeModifier(
			EpdConditionFactor::getObjectSpecificGrade,
			EpdConditionFactor::withObjectSpecificGrade));
		ms.bind(M.ReferenceGrade, new GradeModifier(
			EpdConditionFactor::getReferenceGrade,
			EpdConditionFactor::withReferenceGrade));
		ms.bind(M.Comment, new CommentModifier());

		var add = Actions.create(
			M.Add, Icon.ADD.des(), () -> onAdd(table));
		var rem = Actions.create(
			M.Remove, Icon.DELETE.des(), () -> onRemove(table));
		Actions.bind(table, add, rem);

		if (obj != null) {
			table.setInput(obj.getConditionFactors());
		}
	}

	private void onAdd(TableViewer table) {
		var list = withObject().withConditionFactors();
		var used = EnumSet.noneOf(EpdConditionCategory.class);
		for (var other : list) {
			if (other.getCategory() != null) {
				used.add(other.getCategory());
			}
		}

		// try to set an initial category that was not used yet
		var f = new EpdConditionFactor();
		for (var category : EpdConditionCategory.values()) {
			if (used.contains(category))
				continue;
			f.withCategory(category);
			break;
		}
		if (f.getCategory() == null) {
			f.withCategory(EpdConditionCategory.values()[0]);
		}

		list.add(f);
		table.setInput(list);
		editor.setDirty();
	}

	private void onRemove(TableViewer table) {
		var list = withObject().getConditionFactors();
		if (list.isEmpty())
			return;
		for (var s : Viewers.getAllSelected(table)) {
			if (s instanceof EpdConditionFactor f) {
				list.remove(f);
			}
		}
		table.setInput(list);
		editor.setDirty();
	}

	private static class FactorLabel extends BaseLabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object o, int i) {
			return null;
		}

		@Override
		public String getColumnText(Object o, int i) {
			if (!(o instanceof EpdConditionFactor f))
				return null;
			return switch (i) {
				case 0 -> f.getCategory() != null
					? f.getCategory().value()
					: null;

				case 1 -> Double.toString(f.getValue());

				case 2 -> f.getObjectSpecificGrade() != null
					? f.getObjectSpecificGrade().toString()
					: null;

				case 3 -> f.getReferenceGrade() != null
					? f.getReferenceGrade().toString()
					: null;

				case 4 -> f.getComments() != null
					? App.s(f.getComments())
					: null;

				default -> null;
			};
		}
	}

	private class CategoryModifier extends
		ComboModifier<EpdConditionFactor, EpdConditionCategory> {

		@Override
		protected EpdConditionCategory getItem(EpdConditionFactor f) {
			return f.getCategory();
		}

		@Override
		protected EpdConditionCategory[] getItems(EpdConditionFactor f) {
			return EpdConditionCategory.values();
		}

		@Override
		protected String getText(EpdConditionCategory category) {
			return category != null ? category.value() : null;
		}

		@Override
		protected void setItem(EpdConditionFactor f, EpdConditionCategory cat) {
			if (Objects.equals(f.getCategory(), cat))
				return;
			f.withCategory(cat);
			editor.setDirty();
		}
	}

	private class GradeModifier extends
		ComboModifier<EpdConditionFactor, String> {

		private static final String[] GRADES =
			{"", "0", "1", "2", "3", "4", "5"};

		private final Function<EpdConditionFactor, Integer> getter;
		private final BiConsumer<EpdConditionFactor, Integer> setter;

		GradeModifier(
			Function<EpdConditionFactor, Integer> getter,
			BiConsumer<EpdConditionFactor, Integer> setter
		) {
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected String getItem(EpdConditionFactor f) {
			Integer val = getter.apply(f);
			return val != null ? val.toString() : "";
		}

		@Override
		protected String[] getItems(EpdConditionFactor f) {
			return GRADES;
		}

		@Override
		protected String getText(String grade) {
			return grade;
		}

		@Override
		protected void setItem(EpdConditionFactor f, String grade) {
			Integer val = grade.isEmpty()
				? null
				: Integer.parseInt(grade);
			if (Objects.equals(getter.apply(f), val))
				return;
			setter.accept(f, val);
			editor.setDirty();
		}
	}

	private class CommentModifier extends
		DialogModifier<EpdConditionFactor> {

		@Override
		protected void openDialog(EpdConditionFactor f) {
			var opt = LangTextDialog.openMultiLine(f.getComments());
			opt.ifPresent(comments -> {
				f.withComments(comments);
				editor.setDirty();
			});
		}
	}

}
