package epd.index;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
		add(Ref.of(ds), ds.getClassifications());
	}

	public void add(Ref ref, List<Classification> classes) {
		if (ref == null || !ref.isValid())
			return;
		TypeNode root = getNode(ref.type);
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
		TypeNode root = getNode(ref.type);
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

	/** Get the reference with the same type and UUID from the tree. */
	public Ref find(Ref ref) {
		if (ref == null)
			return null;
		TypeNode root = getNode(ref.type);
		if (root == null)
			return null;
		return root.find(ref);
	}

	/**
	 * Collects all data set references from the index.
	 */
	public Set<Ref> getRefs() {
		Set<Ref> refs = new HashSet<>();
		Queue<Node> queue = new ArrayDeque<>();
		queue.addAll(nodes.values());
		while (!queue.isEmpty()) {
			Node n = queue.poll();
			refs.addAll(n.refs);
			queue.addAll(n.categories);
		}
		return refs;
	}

	public void dump(File file) {
		if (file == null)
			return;
		Gson gson = new Gson();
		try {
			String json = gson.toJson(this);
			Files.writeString(file.toPath(), json,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to write index", e);
		}
	}

	public static Index load(File file) {
		if (file == null || !file.exists())
			return new Index();
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			String json = new String(bytes, StandardCharsets.UTF_8);
			Gson gson = new Gson();
			return gson.fromJson(json, Index.class);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Index.class);
			log.error("failed to read index", e);
			return new Index();
		}
	}
}
