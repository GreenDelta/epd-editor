package app.navi;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

public class DataTypeElement implements NavigationElement {

	private final DataSetType type;
	private final NavigationRoot parent;

	public DataTypeElement(NavigationRoot parent, DataSetType type) {
		this.parent = parent;
		this.type = type;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		if (type == null)
			return "";
		switch (type) {
		case CONTACT:
			return "#Contacts";
		case EXTERNAL_FILE:
			return "#External Files";
		case FLOW:
			return "#Flows";
		case FLOW_PROPERTY:
			return "#Flow Properties";
		case LCIA_METHOD:
			return "#LCIA Methods";
		case PROCESS:
			return "#EPD Data Sets";
		case SOURCE:
			return "#Sources";
		case UNIT_GROUP:
			return "#Unit Groups";
		default:
			return "#Unknown";
		}
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public void update() {
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

}
