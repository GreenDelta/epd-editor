package app.editors.refs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.M;
import app.navi.NavigationTree;
import app.navi.Navigator;
import app.navi.RefElement;
import app.navi.RefTextFilter;
import app.rcp.Icon;
import app.util.Actions;
import app.util.UI;
import app.util.Viewers;

public class RefSelectionDialog extends FormDialog {

	private final DataSetType modelType;
	private final boolean multi;
	private final List<Ref> selection = new ArrayList<>();
	private TreeViewer viewer;
	private Text filterText;

	/// Opens a dialog for selecting a single data set of the given type.
	/// Returns `null` when no data set was selected.
	public static Ref select(DataSetType type) {
		var diag = new RefSelectionDialog(UI.shell(), type, false);
		if (diag.open() != OK || diag.selection.isEmpty())
			return null;
		return diag.selection.getFirst();
	}

	/// Opens a dialog for selecting one or more data sets of the given type.
	/// Returns an empty list when no data set was selected.
	public static List<Ref> selectMultiple(DataSetType type) {
		var diag = new RefSelectionDialog(UI.shell(), type, true);
		if (diag.open() != OK)
			return List.of();
		return List.copyOf(diag.selection);
	}

	private RefSelectionDialog(Shell shell, DataSetType type, boolean multi) {
		super(shell);
		this.modelType = type;
		this.multi = multi;
		setBlockOnOpen(true);
	}

	@Override
	protected void createButtonsForButtonBar(Composite comp) {
		createButton(comp, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(comp, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected void createFormContent(IManagedForm form) {
		FormToolkit tk = form.getToolkit();
		UI.formHeader(form, getTitle());
		Composite body = UI.formBody(form.getForm(), tk);
		UI.gridLayout(body, 1);
		Label filterLabel = UI.formLabel(body, form.getToolkit(), M.Filter);
		filterLabel.setFont(UI.boldFont());
		filterText = UI.formText(body);
		Section section = UI.section(body, tk, "Content");
		addSectionActions(section);
		UI.stretchXY(section);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		createViewer(composite);
	}

	private String getTitle() {
		if (modelType == null)
			return "unknown?";
		return switch (modelType) {
			case CONTACT -> M.Contact;
			case FLOW -> M.Flow;
			case FLOW_PROPERTY -> M.FlowProperty;
			case IMPACT_METHOD -> M.LCIAMethod;
			case PROCESS -> M.EPD;
			case SOURCE -> M.Source;
			case UNIT_GROUP -> M.UnitGroup;
			default -> "unknown?";
		};
	}

	private void createViewer(Composite composite) {
		viewer = NavigationTree.viewer(
			composite, multi ? SWT.MULTI : SWT.SINGLE);
		RefTextFilter filter = new RefTextFilter(filterText, viewer);
		viewer.setFilters(filter);
		UI.stretchXY(viewer.getTree());
		viewer.addSelectionChangedListener(new SelectionChange());
		if (!multi) {
			// in the multi-selection mode the user builds up the selection
			// step by step; a double click on an element would collapse the
			// selection to that element and thus should not close the dialog
			viewer.addDoubleClickListener(new DoubleClick());
		}
		viewer.setInput(Navigator.getTypeRoot(modelType));
	}

	@Override
	protected Point getInitialSize() {
		int width = 600;
		int height = 600;
		Rectangle shellBounds = getShell().getDisplay().getBounds();
		int shellWidth = shellBounds.x;
		int shellHeight = shellBounds.y;
		if (shellWidth > 0 && shellWidth < width)
			width = shellWidth;
		if (shellHeight > 0 && shellHeight < height)
			height = shellHeight;
		return new Point(width, height);
	}

	private void addSectionActions(Section section) {
		Action expand = Actions.create(
				"Expand all",
				Icon.EXPAND.des(),
				() -> viewer.expandAll());
		Action collapse = Actions.create(
				"Collapse all",
				Icon.COLLAPSE.des(),
				() -> viewer.collapseAll());
		Actions.bind(section, expand, collapse);
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point loc = super.getInitialLocation(initialSize);
		int marginTop = (getParentShell().getSize().y - initialSize.y) / 3;
		if (marginTop < 0)
			marginTop = 0;
		return new Point(loc.x, loc.y + marginTop);
	}

	/// Collects the data set references from the current viewer selection.
	/// Elements that are no data set references (like folders) are ignored.
	private void collectSelection() {
		selection.clear();
		for (var e : Viewers.getAllSelected(viewer)) {
			if (e instanceof RefElement refElem && refElem.ref() != null) {
				selection.add(refElem.ref());
			}
		}
	}

	private class SelectionChange implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent evt) {
			collectSelection();
			getButton(IDialogConstants.OK_ID).setEnabled(!selection.isEmpty());
		}
	}

	private class DoubleClick implements IDoubleClickListener {

		@Override
		public void doubleClick(DoubleClickEvent evt) {
			collectSelection();
			if (!selection.isEmpty()) {
				okPressed();
			}
		}
	}
}
