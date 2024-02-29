package app.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.commons.DataSetType;

public enum Icon {

	ADD("add.png"),

	CANCELED("canceled.png"),

	CHECK_TRUE("check_true.png"),

	CHECK_FALSE("check_false.png"),

	COLLAPSE("collapse.png"),

	CONNECTION("connection.png"),

	CONTACT("contact.png"),

	COPY("copy.png"),

	DELETE("delete.png"),

	DELETE_DIS("delete_dis.png"),

	DOCUMENT("document.png"),

	DOWNLOAD("download.png"),

	EPD("epd.png"),

	ERROR("error.png"),

	EXCEL("excel.png"),

	EXPAND("expand.png"),

	EXPORT("export.png"),

	FOLDER("folder.png"),

	IMPORT("import.png"),

	INFO("info.png"),

	LOCATION("location.png"),

	MESSAGE("message.png"),

	METHOD("method.png"),

	NEW_DATA_SET("new_data_set.png"),

	OK("ok.png"),

	OPEN("open.png"),

	PASTE("paste.png"),

	PRODUCT("product.png"),

	QUANTITY("quantity.png"),

	RELOAD("reload.png"),

	SAVE_AS("save_as.png"),

	SEARCH("search.png"),

	SETTINGS("settings.png"),

	SOURCE("source.png"),

	UNIT("unit.png"),

	UP("up.png"),

	UP_DISABLED("up.png"),

	UPLOAD("upload.png"),

	WARNING("warning.png");

	private final String fileName;

	Icon(String fileName) {
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
		return switch (type) {
			case IMPACT_METHOD -> METHOD.img();
			case PROCESS -> EPD.img();
			case CONTACT -> CONTACT.img();
			case SOURCE -> SOURCE.img();
			case FLOW -> PRODUCT.img();
			case FLOW_PROPERTY -> QUANTITY.img();
			case UNIT_GROUP -> UNIT.img();
			default -> null;
		};
	}

	public static ImageDescriptor des(DataSetType type) {
		if (type == null)
			return null;
		return switch (type) {
			case PROCESS -> EPD.des();
			case CONTACT -> CONTACT.des();
			case SOURCE -> SOURCE.des();
			case FLOW -> PRODUCT.des();
			case FLOW_PROPERTY -> QUANTITY.des();
			case UNIT_GROUP -> UNIT.des();
			case IMPACT_METHOD -> METHOD.des();
			case EXTERNAL_FILE -> DOCUMENT.des();
			default -> null;
		};
	}

}
