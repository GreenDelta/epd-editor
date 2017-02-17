package app.navi;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

public class NavigationRoot extends NavigationElement {

	@Override
	public void update() {
		childs.addAll(Arrays.asList(
				new TypeElement(this, DataSetType.PROCESS),
				new TypeElement(this, DataSetType.CONTACT),
				new TypeElement(this, DataSetType.SOURCE),
				new TypeElement(this, DataSetType.FLOW),
				new TypeElement(this, DataSetType.FLOW_PROPERTY),
				new TypeElement(this, DataSetType.UNIT_GROUP),
				new TypeElement(this, DataSetType.LCIA_METHOD),
				new FolderElement(this, FolderType.LOCATION),
				new FolderElement(this, FolderType.CLASSIFICATION),
				new FolderElement(this, FolderType.DOC),
				new ConnectionFolder(this)));
		for (NavigationElement child : getChilds()) {
			child.getChilds();
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
