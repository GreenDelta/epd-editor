package app.store;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.RefTree;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import app.App;
import epd.io.QNameJsonAdapter;

/**
 * Manages the access to cached dependency trees. These trees can be used to
 * search for data set usages, but they need quite some resources in order to
 * create (parsing the XML, searching for the references, etc.).
 */
public final class RefTrees {

	private RefTrees() {
	}

	public static RefTree get(Ref ref) {
		if (ref == null || !ref.isValid())
			return new RefTree();
		var file = cacheFile(ref).orElse(null);

		if (file != null && file.exists()) {
			try (var stream = new FileInputStream(file);
					 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				return new GsonBuilder()
					.registerTypeAdapter(QName.class, new QNameJsonAdapter())
					.create()
					.fromJson(reader, RefTree.class);
			} catch (Exception e) {
				LoggerFactory.getLogger(RefTrees.class)
					.error("failed to read cache file: " + file, e);
			}
		}

		var ds = Data.load(ref);
		var tree = RefTree.create(ds);
		cache(ref, tree);
		return tree;
	}

	public static void remove(Ref ref) {
		if (ref == null || !ref.isValid())
			return;
		var f = cacheFile(ref).orElse(null);
		if (f == null || !f.exists())
			return;
		try {
			Files.delete(f.toPath());
		} catch (IOException e) {
			LoggerFactory.getLogger(RefTrees.class)
				.error("failed to delete file " + f, e);
		}
	}

	public static void cache(IDataSet ds) {
		if (ds == null)
			return;
		Ref ref = Ref.of(ds);
		if (!ref.isValid())
			return;
		var tree = RefTree.create(ds);
		cache(ref, tree);
	}

	private static void cache(Ref ref, RefTree tree) {
		var file = cacheFile(ref).orElse(null);
		if (file == null)
			return;

		try (var fos = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				 var buffer = new BufferedWriter(writer)) {
			new GsonBuilder()
				.registerTypeAdapter(QName.class, new QNameJsonAdapter())
				.create()
				.toJson(tree, buffer);
		} catch (Exception e) {
			LoggerFactory.getLogger(RefTrees.class)
				.error("failed to write cache file: " + file, e);
		}
	}

	private static Optional<File> cacheFile(Ref ref) {
		var dir = new File(
			App.workspaceFolder(),
			"cache/refs/" + ref.getType().name());
		if (!dir.exists()) {
			try {
				Files.createDirectories(dir.toPath());
			} catch (Exception e) {
				LoggerFactory.getLogger(RefTrees.class)
					.error("failed to create cache folder: " + dir, e);
				return Optional.empty();
			}
		}
		// we should always cache the current version only
		// String v = Version.fromString(ref.version).toString();
		return Optional.of(new File(dir, ref.getUUID() + ".json"));
	}

}
