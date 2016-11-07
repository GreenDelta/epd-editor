package app.util.tables;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.rcp.Icon;
import app.util.Actions;
import app.util.Tables;
import app.util.Viewers;

/**
 * Abstract implementation of AbstractViewer for SWT table viewer.
 * 
 * There are three extensions that can be implemented by annotating the methods
 * of implementing classes. To enable creation and removal actions use
 * annotations {@link OnAdd} and {@link OnRemove}. The run methods of each
 * action will call all annotated methods. Implementations are responsible to
 * update the input. To enable drop feature use {@link OnDrop} and specify the
 * type of accepted elements by the input parameter of the annotated method.
 */
public class AbstractTableViewer<T> extends AbstractViewer<T, TableViewer> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private List<Action> actions;
	private ModifySupport<T> cellModifySupport;

	protected AbstractTableViewer(Composite parent) {
		super(parent);
	}

	@Override
	protected TableViewer createViewer(Composite parent) {
		TableViewer viewer = Tables.createViewer(parent, getColumnHeaders(),
				getLabelProvider());
		createActions(viewer);
		if (useColumnHeaders())
			cellModifySupport = new ModifySupport<>(viewer);
		return viewer;
	}

	private void createActions(TableViewer viewer) {
		actions = new ArrayList<>();
		if (supports(OnAdd.class))
			actions.add(Actions.create("#Add", Icon.ADD.des(),
					() -> call(OnAdd.class)));
		if (supports(OnRemove.class)) {
			actions.add(Actions.create("#Remove", Icon.DELETE.des(),
					() -> call(OnRemove.class)));
			Tables.onDeletePressed(viewer, (e) -> call(OnRemove.class));
		}
		// we have to create this array, because we do not want to have the copy
		// action in the section menu
		List<Action> additionalActions = getAdditionalActions();
		Action[] tableActions = new Action[actions.size()
				+ additionalActions.size() + 1];
		for (int i = 0; i < actions.size(); i++)
			tableActions[i] = actions.get(i);
		for (int i = 0; i < additionalActions.size(); i++)
			tableActions[i + actions.size()] = additionalActions.get(i);
		tableActions[tableActions.length - 1] = TableClipboard.onCopy(viewer);
		Actions.bind(viewer, tableActions);
	}

	protected List<Action> getAdditionalActions() {
		return Collections.emptyList();
	}

	protected ModifySupport<T> getModifySupport() {
		return cellModifySupport;
	}

	/**
	 * Subclasses may override this for support of column headers for the table
	 * combo, if null or empty array is returned, the headers are not visible
	 * and the combo behaves like a standard combo
	 */
	protected String[] getColumnHeaders() {
		return null;
	}

	private boolean useColumnHeaders() {
		return getColumnHeaders() != null && getColumnHeaders().length > 0;
	}

	/**
	 * Binds the create and remove actions of the table viewer to the given
	 * section.
	 */
	public void bindTo(Section section) {
		Actions.bind(section, actions.toArray(new Action[actions.size()]));
	}

	@SuppressWarnings("unchecked")
	public List<T> getAllSelected() {
		List<Object> list = Viewers.getAllSelected(getViewer());
		List<T> result = new ArrayList<>();
		for (Object value : list)
			if (!(value instanceof AbstractViewer.Null))
				result.add((T) value);
		return result;
	}

	private boolean supports(Class<? extends Annotation> clazz) {
		for (Method method : this.getClass().getDeclaredMethods())
			if (method.isAnnotationPresent(clazz))
				return true;
		return false;
	}

	private void call(Class<? extends Annotation> clazz) {
		for (Method method : getMethods(clazz))
			try {
				method.setAccessible(true);
				method.invoke(this);
			} catch (Exception e) {
				log.error("Cannot call method for " + clazz.getSimpleName(), e);
			}
	}

	private List<Method> getMethods(Class<? extends Annotation> clazz) {
		List<Method> methods = new ArrayList<>();
		for (Method method : this.getClass().getDeclaredMethods())
			if (method.isAnnotationPresent(clazz))
				methods.add(method);
		return methods;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface OnAdd {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface OnRemove {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface OnDrop {
	}

}
