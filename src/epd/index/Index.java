package epd.index;

import java.util.HashMap;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
