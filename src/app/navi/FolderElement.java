package app.navi;

import java.io.File;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.FileStore;

import app.App;
import app.rcp.Icon;

public class FolderElement extends NavigationElement {

	public final FolderType type;
	private final NavigationElement parent;

	public FolderElement(NavigationElement parent, FolderType type) {
		this.parent = parent;
		this.type = type;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public String getLabel() {
		if (type == null)
			return "?";
		switch (type) {
		case CLASSIFICATION:
			return "#Classifications";
		case LOCATION:
			return "#Locations";
		case DOC:
			return "#Documents";
		default:
			return "?";
		}
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER.img();
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		childs.clear();
		File folder = getFolder();
		if (folder == null || !folder.exists())
			return;
		File[] files = folder.listFiles();
		for (File file : files)
			childs.add(new FileElement(this, file));
	}

	public File getFolder() {
		FileStore store = App.store;
		File root = null;
		if (store == null)
			root = new File("data/ILCD");
		else
			root = store.getRootFolder();
		return new File(root, getFolderName());
	}

	public String getFileExtension() {
		if (type == null)
			return "*.*";
		switch (type) {
		case CLASSIFICATION:
			return "*.xml";
		case DOC:
			return "*.*";
		case LOCATION:
			return "*.*";
		default:
			return "*.*";
		}
	}

	private String getFolderName() {
		if (type == null)
			return "other";
		switch (type) {
		case CLASSIFICATION:
			return "classifications";
		case LOCATION:
			return "locations";
		case DOC:
			return "external_docs";
		default:
			return "other";
		}
	}

}
