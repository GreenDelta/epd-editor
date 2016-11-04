package app.wizards;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.M;
import app.navi.NavigationElement;
import app.navi.NavigationTree;
import app.navi.Navigator;
import app.navi.RefElement;
import app.navi.RefTextFilter;
import app.util.UI;
import app.util.Viewers;

class EpdWizardPage extends WizardPage {

	private Text nameText;
	private Text filterText;
	private Ref flow;
	private TreeViewer productViewer;

	public EpdWizardPage() {
		super(M.CreateANewEPD);
		setTitle(M.CreateANewEPD);
		setDescription(M.CreateANewEPD_Description);
		setPageComplete(false);
	}

	public String getEpdName() {
		return nameText.getText();
	}

	public Ref getFlow() {
		return flow;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = UI.formComposite(parent);
		nameText = UI.formText(composite, M.Name);
		nameText.addModifyListener((e) -> validateInput());
		filterText = UI.formText(composite, M.Filter);
		UI.formLabel(composite, M.Product);
		createProductViewer(new Composite(composite, SWT.NONE));
		setControl(composite);
		setPageComplete(false);
	}

	private void createProductViewer(Composite comp) {
		UI.gridData(comp, true, false);
		comp.setLayout(getViewerLayout());
		productViewer = NavigationTree.viewer(comp);
		UI.gridData(productViewer.getTree(), true, true).heightHint = 200;
		productViewer.addFilter(new RefTextFilter(filterText, productViewer));
		productViewer.setInput(Navigator.getTypeRoot(DataSetType.FLOW));
		productViewer.addSelectionChangedListener((e) -> setSelectedProduct());
	}

	private void setSelectedProduct() {
		NavigationElement e = Viewers.getFirstSelected(productViewer);
		if (!(e instanceof RefElement))
			return;
		RefElement refElem = (RefElement) e;
		flow = refElem.ref;
		validateInput();
	}

	private GridLayout getViewerLayout() {
		GridLayout layout = new GridLayout(1, true);
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		return layout;
	}

	private void validateInput() {
		String name = nameText.getText();
		if (name == null || name.trim().isEmpty()) {
			error("No name selected");
			return;
		}
		if (flow == null) {
			error("No product selected");
			return;
		}
		setPageComplete(true);
		setMessage(null);
	}

	private void error(String string) {
		setMessage(string, DialogPage.ERROR);
		setPageComplete(false);
	}

}
