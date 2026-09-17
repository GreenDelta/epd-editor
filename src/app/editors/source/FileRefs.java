package app.editors.source;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.openlca.commons.Strings;
import org.openlca.ilcd.sources.FileRef;

import app.App;

final class FileRefs {

	static Optional<File> localFileOf(FileRef ref) {
		var file = App.store().getExternalDocument(ref);
		return file != null && file.isFile()
			? Optional.of(file)
			: Optional.empty();
	}

	static boolean isNonAscii(FileRef ref) {
		var file = localFileOf(ref).orElse(null);
		return isNonAscii(file);
	}

	/// Inserts the given UUID before the file extension of the given file name:
	/// `flow_chart.png` becomes `flow_chart_<uuid>.png`. When the name has no
	/// extension, the UUID is appended. This is used to make the names of
	/// external documents unique in the `external_docs` folder.
	static String withUuid(String fileName, String uuid) {
		if (Strings.isBlank(fileName) || Strings.isBlank(uuid))
			return fileName;
		int idx = fileName.lastIndexOf('.');
		return idx <= 0
			? fileName + "_" + uuid
			: fileName.substring(0, idx) + "_" + uuid
				+ fileName.substring(idx);
	}

	/// Checks if the given file reference points to a file that was attached to
	/// the given source data set; the names of such files contain the UUID of
	/// the source.
	static boolean hasSourceUuid(FileRef ref, String sourceUuid) {
		var uri = ref == null ? null : ref.getUri();
		return Strings.isNotBlank(uri)
			&& Strings.isNotBlank(sourceUuid)
			&& uri.contains(sourceUuid);
	}

	// see https://github.com/GreenDelta/epd-editor/issues/39
	static boolean isNonAscii(File file) {
		if (file == null || !file.isFile())
			return false;
		return !StandardCharsets.US_ASCII
			.newEncoder()
			.canEncode(file.getName());
	}
}
