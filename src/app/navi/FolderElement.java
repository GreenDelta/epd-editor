package app.navi;

import java.io.File;

import org.eclipse.swt.graphics.Image;
import org.openlca.ilcd.io.FileStore;

import app.App;
import app.M;
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
		return switch (type) {
			case CLASSIFICATION -> M.Classifications;
			case LOCATION -> M.Locations;
			case DOC -> M.Documents;
		};
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
		var folder = getFolder();
		if (folder == null || !folder.exists())
			return;
		var files = folder.listFiles();
		if (files == null)
			return;
		for (var file : files) {
			childs.add(new FileElement(this, file));
		}
	}

	public File getFolder() {
		FileStore store = App.store();
		File root = store == null
				? new File("data/ILCD")
				: store.getRootFolder();
		return new File(root, getFolderName());
	}

	public String getFileExtension() {
		if (type == null)
			return "*.*";
		if (type == FolderType.CLASSIFICATION) {
			return "*.xml";
		}
		return "*.*";
	}

	private String getFolderName() {
		if (type == null)
			return "other";
		return switch (type) {
			case CLASSIFICATION -> "classifications";
			case LOCATION -> "locations";
			case DOC -> "external_docs";
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FolderElement other))
			return false;
		return this.type == other.type;
	}
}
