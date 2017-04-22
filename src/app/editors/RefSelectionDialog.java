package app.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
import app.navi.NavigationElement;
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
	private TreeViewer viewer;
	private Text filterText;
	private Ref selection;

	public static Ref select(DataSetType type) {
		RefSelectionDialog diag = new RefSelectionDialog(
				UI.shell(), type);
		if (diag.open() == OK)
			return diag.selection;
		return null;
	}

	private RefSelectionDialog(Shell shell, DataSetType type) {
		super(shell);
		this.modelType = type;
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
		filterText = UI.formText(body, SWT.SEARCH);
		Section section = UI.section(body, tk, "#Content");
		addSectionActions(section);
		UI.gridData(section, true, true);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		createViewer(composite);
	}

	private String getTitle() {
		if (modelType == null)
			return "unknown?";
		switch (modelType) {
		case CONTACT:
			return M.Contact;
		case FLOW:
			return M.Flow;
		case FLOW_PROPERTY:
			return M.FlowProperty;
		case LCIA_METHOD:
			return M.LCIAMethod;
		case PROCESS:
			return M.EPD;
		case SOURCE:
			return M.Source;
		case UNIT_GROUP:
			return M.UnitGroup;
		default:
			return "unknown?";
		}
	}

	private void createViewer(Composite composite) {
		viewer = NavigationTree.viewer(composite);
		RefTextFilter filter = new RefTextFilter(filterText, viewer);
		viewer.setFilters(new ViewerFilter[] { filter });
		UI.gridData(viewer.getTree(), true, true);
		viewer.addSelectionChangedListener(new SelectionChange());
		viewer.addDoubleClickListener(new DoubleClick());
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
				"#Expand all",
				Icon.EXPAND.des(),
				() -> viewer.expandAll());
		Action collapse = Actions.create(
				"#Collapse all",
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

	private class SelectionChange implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent evt) {
			NavigationElement e = Viewers.getFirst(evt.getSelection());
			if (!(e instanceof RefElement))
				return;
			RefElement refElem = (RefElement) e;
			selection = refElem.ref;
			getButton(IDialogConstants.OK_ID).setEnabled(selection != null);
		}
	}

	private class DoubleClick implements IDoubleClickListener {

		@Override
		public void doubleClick(DoubleClickEvent evt) {
			NavigationElement e = Viewers.getFirst(evt.getSelection());
			if (!(e instanceof RefElement))
				return;
			RefElement refElem = (RefElement) e;
			selection = refElem.ref;
			okPressed();
		}
	}
}
