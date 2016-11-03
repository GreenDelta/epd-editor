package app.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

public enum Icon {

	CONTACT("contact.png"),

	EPD("epd.png"),

	FOLDER("folder.png"),

	IMPORT("import.png"),

	PRODUCT("product.png"),

	QUANTITY("quantity.png"),

	SOURCE("source.png"),

	UNIT("unit.png");

	private final String fileName;

	private Icon(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public ImageDescriptor des() {
		return ImageManager.getImageDescriptor(this);
	}

	public Image img() {
		return ImageManager.getImage(this);
	}

	public static Image img(DataSetType type) {
		if (type == null)
			return null;
		switch (type) {
		case PROCESS:
			return EPD.img();
		case CONTACT:
			return CONTACT.img();
		case SOURCE:
			return SOURCE.img();
		case FLOW:
			return PRODUCT.img();
		case FLOW_PROPERTY:
			return QUANTITY.img();
		case UNIT_GROUP:
			return UNIT.img();
		default:
			return null;
		}
	}

}
