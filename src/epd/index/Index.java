package epd.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.model.Ref;
import epd.util.Strings;

public class Index {

	public List<Ref> processes = new ArrayList<>();

	public static Index create(FileStore store, String lang) {
		Index idx = new Index();
		if (store == null)
			return idx;
		idx.collectRefs(store.getFolder(Process.class), idx.processes, lang);
		return idx;
	}

	private void collectRefs(File dir, List<Ref> list, String lang) {
		RefCollector collector = new RefCollector(list, lang);
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			for (File file : dir.listFiles()) {
				if (!file.getName().toLowerCase().endsWith(".xml")) {
					continue;
				}
				parser.parse(file, collector);
			}
			Collections.sort(list,
					(r1, r2) -> Strings.compare(r1.name, r2.name));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse XML files", e);
		}
	}
}
