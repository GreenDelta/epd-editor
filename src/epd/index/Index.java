package epd.index;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import app.store.RefExt;
import epd.io.QNameJsonAdapter;
import epd.util.Strings;

public class Index {

	private final HashMap<DataSetType, TypeNode> nodes = new HashMap<>();

	public TypeNode getNode(DataSetType type) {
		TypeNode node = nodes.get(type);
		if (node == null) {
			node = new TypeNode(type);
			nodes.put(type, node);
		}
		return node;
	}

	public void add(IDataSet ds) {
		if (ds == null)
			return;
		var ref = Ref.of(ds);
		if (!ref.isValid())
			return;
		if (ds instanceof Process epd) {
			RefExt.add(epd, ref);
		}
		add(ref, DataSets.getClassifications(ds));
	}

	public void add(Ref ref, List<Classification> classes) {
		if (ref == null || !ref.isValid())
			return;
		TypeNode root = getNode(ref.getType());
		List<CategoryNode> catNodes = root.syncCategories(classes);
		if (catNodes.isEmpty()) {
			root.refs.add(ref);
			return;
		}
		for (CategoryNode catNode : catNodes) {
			catNode.refs.add(ref);
		}
	}

	public void remove(Ref ref) {
		if (ref == null)
			return;
		TypeNode root = getNode(ref.getType());
		if (root == null)
			return;
		root.remove(ref);
	}

	public void removeEmptyCategories() {
		for (TypeNode node : nodes.values()) {
			if (node == null)
				continue;
			node.removeEmptyCategories();
		}
	}

	/**
	 * Get the reference with the same type and UUID from the tree.
	 */
	public Ref find(Ref ref) {
		if (ref == null)
			return null;
		TypeNode root = getNode(ref.getType());
		if (root == null)
			return null;
		return root.find(ref);
	}

	/**
	 * Collects all data set references from the index.
	 */
	public Set<Ref> getRefs() {
		Set<Ref> refs = new HashSet<>();
		var queue = new ArrayDeque<Node>(nodes.values());
		while (!queue.isEmpty()) {
			var node = queue.poll();
			refs.addAll(node.refs);
			queue.addAll(node.categories);
		}
		return refs;
	}

	public void dump(File file) {
		if (file == null)
			return;
		try {
			var json = new GsonBuilder()
				.registerTypeAdapter(QName.class, new QNameJsonAdapter())
				.setPrettyPrinting()
				.create()
				.toJson(this);
			Files.writeString(file.toPath(), json,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to write index", e);
		}
	}

	public static Index load(File file) {
		if (file == null || !file.exists())
			return new Index();
		try {
			var bytes = Files.readAllBytes(file.toPath());
			var json = new String(bytes, StandardCharsets.UTF_8);
			var gson = new GsonBuilder()
				.registerTypeAdapter(QName.class, new QNameJsonAdapter())
				.registerTypeAdapter(DataSetType.class, new DataSetTypeAdapter())
				.create();
			return gson.fromJson(json, Index.class);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Index.class);
			log.error("failed to read index", e);
			return new Index();
		}
	}

	/**
	 * We need a specific type adapter for de-serializing items of the
	 * {@code DataSetType} enumeration. This is because we use it as keys in maps,
	 * and it is also a field of instances of type {@code Ref}. Gson makes
	 * different things here: when it is serialized as a key in a map, the
	 * {@code toString} method is used; but when it is serialized as a field,
	 * the {@code name} method is used to convert an enum-item to a string.
	 * However, when de-serializing it, the {@code name} seems to be used in both
	 * cases. Thus, we need to attach an adapter that supports both cases.
	 */
	private static class DataSetTypeAdapter
		implements JsonDeserializer<DataSetType> {

		@Override
		public DataSetType deserialize(
			JsonElement elem, Type type, JsonDeserializationContext ctx
		) throws JsonParseException {

			if (!DataSetType.class.equals(type))
				return new Gson().fromJson(elem, type);
			if (elem == null || !elem.isJsonPrimitive())
				return null;
			var prim = elem.getAsJsonPrimitive();
			if (!prim.isString())
				return null;

			var s = prim.getAsString();
			if (Strings.nullOrEmpty(s))
				return null;

			try {
				// first try by item-value
				var dsType = DataSetType.fromValue(s).orElse(null);
				if (dsType != null)
					return dsType;
				// then try by item-name
				return DataSetType.valueOf(s);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
