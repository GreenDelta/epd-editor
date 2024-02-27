package app.editors;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.lists.Category;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.CategorySystem;
import org.openlca.ilcd.lists.ContentType;

import app.M;
import app.rcp.Icon;
import app.store.CategorySystems;
import app.util.UI;
import app.util.Viewers;

public class CategoryDialog extends FormDialog {

	private final ContentType type;
	private final List<CategorySystem> systems;

	private TreeViewer treeViewer;
	private CategorySystem selectedSystem;
	private Category selectedCategory;

	public CategoryDialog(DataSetType type) {
		super(UI.shell());
		this.systems = CategorySystems.get();
		this.type = mapType(type);
	}

	private ContentType mapType(DataSetType type) {
		if (type == null)
			return null;
		return switch (type) {
			case CONTACT -> ContentType.CONTACT;
			case FLOW -> ContentType.FLOW;
			case FLOW_PROPERTY -> ContentType.FLOW_PROPERTY;
		case IMPACT_METHOD -> ContentType.LCIA_METHOD;
			case PROCESS -> ContentType.PROCESS;
			case SOURCE -> ContentType.SOURCE;
			case UNIT_GROUP -> ContentType.UNIT_GROUP;
			default -> null;
		};
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	public Classification getSelection() {
		if (selectedSystem == null || selectedCategory == null)
			return null;
		Classification classification = new Classification();
		classification.withName(selectedSystem.getName());
		Map<Category, Category> parentMap = new IdentityHashMap<>();
		fillParentMap(getRootCategories(selectedSystem), parentMap);
		Stack<Category> path = getPath(selectedCategory, parentMap);
		int i = 0;
		while (!path.isEmpty()) {
			Category c = path.pop();
			org.openlca.ilcd.commons.Category clazz = new org.openlca.ilcd.commons.Category();
			clazz.withClassId(c.getId())
				.withLevel(i)
				.withValue(c.getName());
			classification.withCategories().add(clazz);
			i++;
		}
		return classification;
	}

	private void fillParentMap(List<Category> rootCategories,
			Map<Category, Category> parentMap) {
		for (Category root : rootCategories) {
			for (Category child : root.getCategories())
				parentMap.put(child, root);
			fillParentMap(root.getCategories(), parentMap);
		}
	}

	private Stack<Category> getPath(Category category,
			Map<Category, Category> parentMap) {
		Stack<Category> stack = new Stack<>();
		stack.push(category);
		Category parent = parentMap.get(category);
		while (parent != null) {
			stack.push(parent);
			parent = parentMap.get(parent);
		}
		return stack;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		getShell().setText(M.SelectACategory);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(mform.getForm(), tk);
		createCombo(tk, body);
		createTree(body);
	}

	private void createCombo(FormToolkit tk, Composite body) {
		Composite comp = UI.formComposite(body, tk);
		UI.gridData(comp, true, false);
		UI.formLabel(comp, tk, M.ClassificationSystem);
		ComboViewer combo = new ComboViewer(comp);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		UI.gridData(combo.getControl(), true, false);
		combo.setLabelProvider(new ComboLabel());
		combo.setInput(systems);
		if (!systems.isEmpty()) {
			selectedSystem = systems.get(0);
			combo.setSelection(new StructuredSelection(selectedSystem));
		}
		combo.addSelectionChangedListener((e) -> {
			selectedSystem = Viewers.getFirstSelected(combo);
			if (selectedSystem == null)
				return;
			treeViewer.setInput(selectedSystem);
		});
	}

	private void createTree(Composite body) {
		treeViewer = new TreeViewer(body);
		UI.gridData(treeViewer.getControl(), true, true);
		treeViewer.setContentProvider(new TreeContent());
		treeViewer.setLabelProvider(new TreeLabel());
		if (!systems.isEmpty())
			treeViewer.setInput(systems.get(0));
		treeViewer.addSelectionChangedListener((e) -> {
			selectedCategory = Viewers.getFirstSelected(treeViewer);
		});
	}

	private List<Category> getRootCategories(CategorySystem system) {
		if (system == null)
			return Collections.emptyList();
		for (CategoryList list : system.getCategories()) {
			if (list.getType() == type)
				return list.getCategories();
		}
		return Collections.emptyList();
	}

	private static class ComboLabel extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (!(element instanceof CategorySystem system))
				return null;
			return system.getName() != null ? system.getName() : "<no name>";
		}

	}

	private class TreeContent implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof CategorySystem system))
				return new Object[0];
			List<Category> categories = getRootCategories(system);
			return categories.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (!(parentElement instanceof Category category))
				return new Object[0];
			return category.getCategories().toArray();
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (!(element instanceof Category category))
				return false;
			return !category.getCategories().isEmpty();
		}
	}

	private static class TreeLabel extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return Icon.FOLDER.img();
		}

		@Override
		public String getText(Object element) {
			if (!(element instanceof Category category))
				return null;
			return category.getName();
		}
	}

}
