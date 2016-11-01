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
				new DataTypeElement(this, DataSetType.PROCESS),
				new DataTypeElement(this, DataSetType.CONTACT),
				new DataTypeElement(this, DataSetType.SOURCE),
				new DataTypeElement(this, DataSetType.FLOW),
				new DataTypeElement(this, DataSetType.FLOW_PROPERTY),
				new DataTypeElement(this, DataSetType.UNIT_GROUP));
		return childs;
	}

	@Override
	public void update() {
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
