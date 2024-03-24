package app.editors.source;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

	// see https://github.com/GreenDelta/epd-editor/issues/39
	static boolean isNonAscii(File file) {
		if (file == null || !file.isFile())
			return false;
		return !StandardCharsets.US_ASCII
			.newEncoder()
			.canEncode(file.getName());
	}


}
