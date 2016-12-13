package app.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

public enum Icon {

	ADD("add.png"),

	CHECK_TRUE("check_true.png"),

	CHECK_FALSE("check_false.png"),

	COLLAPSE("collapse.png"),

	CONTACT("contact.png"),

	COPY("copy.png"),

	DELETE("delete.png"),

	DELETE_DIS("delete_dis.png"),

	DOCUMENT("document.png"),

	EPD("epd.png"),

	EXPAND("expand.png"),

	EXPORT("export.png"),

	FOLDER("folder.png"),

	IMPORT("import.png"),

	PASTE("paste.png"),

	PRODUCT("product.png"),

	QUANTITY("quantity.png"),

	SOURCE("source.png"),

	UNIT("unit.png"),

	UP("up.png"),

	UP_DISABLED("up.png");

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

	public static ImageDescriptor des(DataSetType type) {
		if (type == null)
			return null;
		switch (type) {
		case PROCESS:
			return EPD.des();
		case CONTACT:
			return CONTACT.des();
		case SOURCE:
			return SOURCE.des();
		case FLOW:
			return PRODUCT.des();
		case FLOW_PROPERTY:
			return QUANTITY.des();
		case UNIT_GROUP:
			return UNIT.des();
		default:
			return null;
		}
	}

}
