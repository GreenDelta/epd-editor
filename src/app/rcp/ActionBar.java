package app.rcp;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ActionBar extends ActionBarAdvisor {

	private IWorkbenchAction save;
	private IWorkbenchAction saveAs;
	private IWorkbenchAction saveAll;

	public ActionBar(IActionBarConfigurer conf) {
		super(conf);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		save = ActionFactory.SAVE.create(window);
		saveAs = ActionFactory.SAVE_AS.create(window);
		saveAll = ActionFactory.SAVE_ALL.create(window);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		super.fillCoolBar(coolBar);
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		toolbar.add(save);
		toolbar.add(saveAs);
		toolbar.add(saveAll);
	}

}
