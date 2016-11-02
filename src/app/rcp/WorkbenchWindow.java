package app.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class WorkbenchWindow extends WorkbenchWindowAdvisor {

	public WorkbenchWindow(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer conf) {
		return new ActionBar(conf);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer conf = getWindowConfigurer();
		conf.setInitialSize(new Point(800, 600));
		conf.setShowCoolBar(true);
		conf.setShowStatusLine(true);
		conf.setShowProgressIndicator(true);
		conf.setShowMenuBar(true);
		conf.setTitle("EPD - Editor 2.0 \u03B1");
	}

	@Override
	public void postWindowClose() {
		ImageManager.dispose();
	}
}
