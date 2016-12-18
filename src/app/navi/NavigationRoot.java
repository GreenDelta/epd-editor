package app.navi;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

public class NavigationRoot implements NavigationElement {

	private List<NavigationElement> childs;

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		childs = Arrays.asList(
				new TypeElement(this, DataSetType.PROCESS),
				new TypeElement(this, DataSetType.CONTACT),
				new TypeElement(this, DataSetType.SOURCE),
				new TypeElement(this, DataSetType.FLOW),
				new TypeElement(this, DataSetType.FLOW_PROPERTY),
				new TypeElement(this, DataSetType.UNIT_GROUP),
				new FolderElement(this, FolderType.LOCATION),
				new FolderElement(this, FolderType.CLASSIFICATION),
				new FolderElement(this, FolderType.DOC),
				new ConnectionFolder(this));
		return childs;
	}

	@Override
	public void update() {
		for (NavigationElement child : getChilds()) {
			child.update();
		}
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public NavigationElement getParent() {
		return null;
	}

}
