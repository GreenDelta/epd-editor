package app.store.indata;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epd.refs.RefFetch;

/// Reads the data sets from the zip file of the InData reference data. This
/// data is not stored in the structure of an ILCD package which means that we
/// cannot use a `ZipStore` here. Instead, we scan all entries that end with
/// `.xml` and detect the type of data set from its root element (see
/// [RefFetch]). Entries that are not data sets are skipped.
public class InDataArchive implements Closeable {

	/// A data set entry of the archive.
	public record Entry(String name, Ref ref) {
	}

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ZipFile zip;

	private InDataArchive(ZipFile zip) {
		this.zip = zip;
	}

	/// Opens the given zip file.
	public static InDataArchive open(File file) throws IOException {
		return new InDataArchive(new ZipFile(file));
	}

	/// Collects the entries of the archive that are data sets.
	public List<Entry> dataSets() {
		var entries = new ArrayList<Entry>();
		var it = zip.entries();
		while (it.hasMoreElements()) {
			var entry = it.nextElement();
			if (entry.isDirectory() || !isXml(entry.getName()))
				continue;
			try {
				var ref = readRef(entry);
				if (ref == null || !ref.isValid()) {
					log.info("skip non data set entry: {}", entry.getName());
					continue;
				}
				entries.add(new Entry(entry.getName(), ref));
			} catch (Exception e) {
				log.error("failed to read entry {}", entry.getName(), e);
			}
		}
		return entries;
	}

	/// Returns the raw data of the given entry.
	public byte[] read(Entry entry) throws IOException {
		if (entry == null)
			return new byte[0];
		var zipEntry = zip.getEntry(entry.name());
		if (zipEntry == null)
			return new byte[0];
		try (var stream = zip.getInputStream(zipEntry)) {
			return stream.readAllBytes();
		}
	}

	private Ref readRef(ZipEntry entry) throws IOException {
		try (var stream = zip.getInputStream(entry)) {
			return RefFetch.get(stream).orElse(null);
		}
	}

	private boolean isXml(String name) {
		return name != null
			&& name.toLowerCase().endsWith(".xml");
	}

	@Override
	public void close() {
		try {
			zip.close();
		} catch (Exception e) {
			log.error("failed to close archive", e);
		}
	}
}
