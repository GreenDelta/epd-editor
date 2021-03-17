package epd.conversion;

import java.nio.file.Files;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.FileStore;

class Tests {

	static void withStore(Consumer<DataStore> fn) {
		try {
			var dir = Files.createTempDirectory("_epd_test").toFile();
			try (var store = new FileStore(dir)) {
				fn.accept(store);
			}
			FileUtils.deleteDirectory(dir);
			// System.out.println(dir.getAbsolutePath());
		} catch (Exception e) {
			throw new RuntimeException("failed to test with file store", e);
		}
	}

}
