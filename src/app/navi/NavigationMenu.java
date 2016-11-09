package app.navi;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.openlca.ilcd.commons.DataSetType;

import app.M;
import app.navi.actions.ListFileImport;
import app.navi.actions.RefDeleteAction;
import app.util.Actions;
import app.util.Viewers;
import app.wizards.EpdWizard;

public class NavigationMenu extends CommonActionProvider {

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ActionContext con = getContext();
		IStructuredSelection s = (IStructuredSelection) con
				.getSelection();
		List<NavigationElement> elements = Viewers.getAll(s);
		if (elements.isEmpty())
			return;
		NavigationElement first = elements.get(0);
		if (first instanceof TypeElement) {
			TypeElement e = (TypeElement) first;
			if (e.type == DataSetType.PROCESS)
				menu.add(Actions.create(M.NewEPD, () -> EpdWizard.open()));
		}
		if (first instanceof ListFolderElement) {
			ListFolderElement e = (ListFolderElement) first;
			menu.add(new ListFileImport(e));
		}
		if (first instanceof RefElement) {
			RefElement e = (RefElement) first;
			menu.add(new RefDeleteAction(e));
		}
	}
}
