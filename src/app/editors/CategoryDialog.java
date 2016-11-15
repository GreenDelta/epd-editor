package app.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.JAXB;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.UI;
import app.util.Viewers;

public class CategoryDialog extends FormDialog {

	private final ContentType type;

	private Logger log = LoggerFactory.getLogger(getClass());
	private List<CategorySystem> systems = new ArrayList<>();
	private TreeViewer treeViewer;
	private CategorySystem selectedSystem;
	private Category selectedCategory;

	public CategoryDialog(DataSetType type) {
		super(UI.shell());
		this.type = mapType(type);
	}

	private ContentType mapType(DataSetType type) {
		if (type == null)
			return null;
		switch (type) {
		case CONTACT:
			return ContentType.CONTACT;
		case FLOW:
			return ContentType.FLOW;
		case FLOW_PROPERTY:
			return ContentType.FLOW_PROPERTY;
		case LCIA_METHOD:
			return ContentType.LCIA_METHOD;
		case PROCESS:
			return ContentType.PROCESS;
		case SOURCE:
			return ContentType.SOURCE;
		case UNIT_GROUP:
			return ContentType.UNIT_GROUP;
		default:
			return null;
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	public Classification getSelection() {
		if (selectedSystem == null || selectedCategory == null)
			return null;
		Classification classification = new Classification();
		classification.name = selectedSystem.name;
		Map<Category, Category> parentMap = new IdentityHashMap<>();
		fillParentMap(getRootCategories(selectedSystem), parentMap);
		Stack<Category> path = getPath(selectedCategory, parentMap);
		int i = 0;
		while (!path.isEmpty()) {
			Category c = path.pop();
			org.openlca.ilcd.commons.Category clazz = new org.openlca.ilcd.commons.Category();
			clazz.classId = c.id;
			clazz.level = i;
			clazz.value = c.name;
			classification.categories.add(clazz);
			i++;
		}
		return classification;
	}

	private void fillParentMap(List<Category> rootCategories,
			Map<Category, Category> parentMap) {
		for (Category root : rootCategories) {
			for (Category child : root.category)
				parentMap.put(child, root);
			fillParentMap(root.category, parentMap);
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
		readSystems();
		getShell().setText(M.SelectACategory);
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(mform.getForm(), toolkit);
		createCombo(toolkit, body);
		createTree(body);
	}

	private void createCombo(FormToolkit toolkit, Composite body) {
		Composite composite = UI.formComposite(body, toolkit);
		UI.gridData(composite, true, false);
		UI.formLabel(composite, toolkit, M.ClassificationSystem);
		ComboViewer combo = new ComboViewer(composite);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		UI.gridData(combo.getControl(), true, false);
		combo.setLabelProvider(new ComboLabel());
		combo.setInput(systems);
		if (systems.size() > 0) {
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
		if (systems.size() > 0)
			treeViewer.setInput(systems.get(0));
		treeViewer.addSelectionChangedListener((e) -> {
			selectedCategory = Viewers.getFirstSelected(treeViewer);
		});
	}

	private void readSystems() {
		File dir = getClassificationDir();
		if (dir == null || !dir.exists())
			return;
		for (File file : dir.listFiles()) {
			try {
				CategorySystem system = JAXB.unmarshal(file,
						CategorySystem.class);
				systems.add(system);
			} catch (Exception e) {
				log.error("failed to parse category file " + file, e);
			}
		}
	}

	private File getClassificationDir() {
		try {
			File rootDir = App.store.getRootFolder();
			if (!rootDir.exists())
				return null;
			return new File(rootDir, "classifications");
		} catch (Exception e) {
			return null;
		}
	}

	private List<Category> getRootCategories(CategorySystem system) {
		if (system == null)
			return Collections.emptyList();
		for (CategoryList list : system.categories) {
			if (list.type == type)
				return list.categories;
		}
		return Collections.emptyList();
	}

	private class ComboLabel extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (!(element instanceof CategorySystem))
				return null;
			CategorySystem system = (CategorySystem) element;
			return system.name != null ? system.name : "<no name>";
		}

	}

	private class TreeContent implements ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof CategorySystem))
				return new Object[0];
			CategorySystem system = (CategorySystem) inputElement;
			List<Category> categories = getRootCategories(system);
			return categories.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (!(parentElement instanceof Category))
				return new Object[0];
			Category category = (Category) parentElement;
			return category.category.toArray();
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (!(element instanceof Category))
				return false;
			Category category = (Category) element;
			return !category.category.isEmpty();
		}
	}

	private class TreeLabel extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return Icon.FOLDER.img();
		}

		@Override
		public String getText(Object element) {
			if (!(element instanceof Category))
				return null;
			Category category = (Category) element;
			return category.name;
		}
	}

}
