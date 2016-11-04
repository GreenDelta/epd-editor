package epd.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.openlca.ilcd.io.FileStore;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.io.conversion.Converter;
import epd.model.EpdDataSet;
import epd.model.EpdDescriptor;

public class EpdStore implements Closeable {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static String lang = "en";

	public final File baseDir;
	public final FileStore ilcdStore;

	public EpdStore(String folder) {
		baseDir = new File(folder);
		if (!baseDir.exists())
			baseDir.mkdirs();
		ilcdStore = new FileStore(baseDir);
	}

	public EpdStore(File dir) {
		baseDir = dir;
		if (!baseDir.exists())
			baseDir.mkdirs();
		ilcdStore = new FileStore(baseDir);
	}

	@Override
	public void close() throws IOException {
		log.trace("close EPD store");
	}

	public boolean delete(EpdDescriptor descriptor) {
		if (descriptor == null)
			return false;
		try {
			log.trace("delete EPD {}", descriptor);
			return ilcdStore.delete(Process.class, descriptor.refId);
		} catch (Exception e) {
			log.error("failed to delete EPD " + descriptor, e);
			return false;
		}
	}

	public boolean contains(EpdDescriptor descriptor) {
		if (descriptor == null)
			return false;
		try {
			return ilcdStore.contains(Process.class, descriptor.refId);
		} catch (Exception e) {
			log.error("failed to check if EPD is available: " + descriptor, e);
			return false;
		}
	}

	public EpdDataSet open(EpdDescriptor descriptor) {
		try {
			log.trace("open EPD data set {}", descriptor);
			Process process = ilcdStore.get(Process.class,
					descriptor.refId);
			MappingConfig config = Configs
					.getMappingConfig(ilcdStore.getRootFolder());
			String[] langs = new String[] { lang, "en" };
			EpdDataSet dataSet = Converter.convert(process, config, langs);
			Converter.readProductData(dataSet, ilcdStore);
			return dataSet;
		} catch (Exception e) {
			log.error("failed to open EPD data set " + descriptor, e);
			return null;
		}
	}

	public void save(EpdDataSet dataSet) {
		try {
			log.trace("update EPD data set {}", dataSet);
			MappingConfig config = Configs
					.getMappingConfig(ilcdStore.getRootFolder());
			Process process = Converter.convert(dataSet, config);
			ilcdStore.put(process, process.processInfo.dataSetInfo.uuid);
			Converter.writeProductData(dataSet, ilcdStore);
		} catch (Exception e) {
			log.error("failed to save EPD data set " + dataSet, e);
		}
	}

}
