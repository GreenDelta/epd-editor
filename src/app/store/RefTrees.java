package app.store;

import java.io.File;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.RefTree;

import app.App;

/**
 * Manages the access to cached dependency trees. These trees can be used to
 * search for data set usages but they need quite some resources in order to
 * create (parsing the XML, searching for the references, etc.).
 */
public final class RefTrees {

	private RefTrees() {
	}

	public static RefTree get(Ref ref) {
		if (ref == null || !ref.isValid())
			return new RefTree();
		File file = cacheFile(ref);
		if (file.exists()) {
			RefTree tree = Json.read(file, RefTree.class);
			if (tree != null)
				return tree;
		}
		IDataSet ds = Data.load(ref);
		RefTree tree = RefTree.create(ds);
		cache(ref, tree);
		return tree;
	}

	public static void remove(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		File f = cacheFile(ref);
		if (f.exists()) {
			f.delete();
		}
	}

	public static void cache(IDataSet ds) {
		if (ds == null)
			return;
		Ref ref = Ref.of(ds);
		if (!ref.isValid())
			return;
		RefTree tree = RefTree.create(ds);
		cache(ref, tree);
	}

	private static void cache(Ref ref, RefTree tree) {
		File file = cacheFile(ref);
		Json.write(tree, file);
	}

	private static File cacheFile(Ref ref) {
		File dir = new File(
				App.workspaceFolder(),
				"cache/refs/" + ref.type.name());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// we should always cache the current version only
		// String v = Version.fromString(ref.version).toString();
		return new File(dir, ref.uuid + ".json");
	}

}
