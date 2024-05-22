package app.navi;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import epd.util.Strings;

/**
 * A class for filtering model elements from a navigation tree via a text
 * filter. The filter directly registers a listener on the text field.
 */
public class RefTextFilter extends ViewerFilter {

	private final Text filterText;

	public RefTextFilter(Text filterText, final TreeViewer viewer) {
		this.filterText = filterText;
		filterText.addModifyListener((e) -> {
			viewer.refresh();
			expand(viewer);
		});
	}

	private void expand(TreeViewer viewer) {
		TreeItem[] items = viewer.getTree().getItems();
		while (items != null && items.length > 0) {
			TreeItem next = items[0];
			next.setExpanded(true);
			for (int i = 1; i < items.length; i++)
				items[i].setExpanded(false);
			items = next.getItems();
			viewer.refresh();
		}
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		String text = filterText.getText();
		if (text == null || text.trim().isEmpty())
			return true;
		text = text.trim().toLowerCase();
		return select((NavigationElement<?>) element, text);
	}

	private boolean select(NavigationElement<?> elem, String text) {
		if (elem instanceof RefElement e) {
			String label = e.getLabel();
			return !Strings.nullOrEmpty(label)
				&& label.toLowerCase().contains(text);
		}
		for (var child : elem.getChilds())
			if (select(child, text))
				return true;
		return false;
	}
}
