package app.navi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

import app.rcp.Icon;
import epd.model.Ref;

public class DataTypeElement implements NavigationElement {

	private final DataSetType type;
	private final NavigationRoot parent;

	public DataTypeElement(NavigationRoot parent, DataSetType type) {
		this.parent = parent;
		this.type = type;
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (type != DataSetType.PROCESS)
			return Collections.emptyList();
		List<NavigationElement> childs = new ArrayList<>();
		for (Ref ref : Navigator.index.processes) {
			DataRefElement e = new DataRefElement(ref);
			childs.add(e);
		}
		return childs;
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
			return "#Products";
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
		return Icon.img(type);
	}

	@Override
	public void update() {
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

}
