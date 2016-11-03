package epd.index;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import epd.model.Ref;

public class Index {

	private HashMap<DataSetType, TypeNode> nodes = new HashMap<>();

	public TypeNode getNode(DataSetType type) {
		TypeNode node = nodes.get(type);
		if (node == null) {
			node = new TypeNode(type);
			nodes.put(type, node);
		}
		return node;
	}

	public void add(IDataSet ds, String lang) {
		if (ds == null)
			return;
		Ref ref = Ref.of(ds, lang);
		TypeNode root = getNode(ds.getDataSetType());
		List<CategoryNode> catNodes = root.syncCategories(ds);
		if (catNodes.isEmpty()) {
			root.refs.add(ref);
			return;
		}
		for (CategoryNode catNode : catNodes)
			catNode.refs.add(ref);
	}

	public static Index create(FileStore store, String lang) {
		Index idx = new Index();
		if (store == null)
			return idx;
		try {
			store.iterator(Process.class).forEachRemaining(
					d -> idx.add(d, lang));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Index.class);
			log.error("failed to index data sets", e);
		}
		return idx;
	}

	public void dump(File file) {
		if (file == null)
			return;
		Gson gson = new Gson();
		try {
			String json = gson.toJson(this);
			byte[] bytes = json.getBytes("utf-8");
			Files.write(file.toPath(), bytes,
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
			String json = new String(bytes, "utf-8");
			Gson gson = new Gson();
			return gson.fromJson(json, Index.class);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Index.class);
			log.error("failed to read index", e);
			return new Index();
		}
	}
}
