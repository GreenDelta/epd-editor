package app.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import app.navi.Navigator;

public class Perspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		IFolderLayout naviLayout = layout.createFolder("Navigation",
				IPageLayout.LEFT, 0.31f, editorArea);
		naviLayout.addView(Navigator.ID);
		IViewLayout vLayout = layout.getViewLayout(Navigator.ID);
		vLayout.setCloseable(false);
		vLayout.setMoveable(false);
	}
}
