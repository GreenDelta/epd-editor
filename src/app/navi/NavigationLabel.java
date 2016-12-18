package app.navi;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

public class NavigationLabel extends ColumnLabelProvider implements
		ICommonLabelProvider {

	private Font boldFont;
	private FontRegistry fontReg;

	@Override
	public String getText(Object element) {
		if (!(element instanceof NavigationElement))
			return super.getText(element);
		else
			return ((NavigationElement) element).getLabel();
	}

	@Override
	public Image getImage(Object element) {
		if (!(element instanceof NavigationElement))
			return super.getImage(element);
		else
			return ((NavigationElement) element).getImage();
	}

	@Override
	public String getDescription(Object element) {
		return null;
	}

	@Override
	public void restoreState(IMemento aMemento) {
	}

	@Override
	public void saveState(IMemento aMemento) {
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		fontReg = new FontRegistry();
		Display display = Display.getCurrent();
		if (display == null)
			return;
		Font sysFont = display.getSystemFont();
		String fontName = sysFont.getFontData()[0].getName();
		boldFont = fontReg.getBold(fontName);
	}

	@Override
	public Font getFont(Object obj) {
		if (obj instanceof TypeElement
				|| obj instanceof FolderElement
				|| obj instanceof ConnectionFolder)
			return boldFont;
		return null;
	}

}
