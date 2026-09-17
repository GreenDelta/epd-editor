package app.store.indata;

import java.io.File;

/// Describes the source from which the InData reference data (master data)
/// should be imported: either the zip file of the InData GitHub repository or
/// a zip file that was downloaded before.
public record InDataSource(String url, File zipFile) {

	/// The URL of the zip file of the main branch of the InData repository.
	/// Note that the `.git` URL of the repository cannot be opened as a zip
	/// file directly.
	public static final String GITHUB_URL = "https://github.com/InDataWG/"
		+ "ILCD-EPD-Master-Data/archive/refs/heads/main.zip";

	public static InDataSource ofUrl(String url) {
		return new InDataSource(url, null);
	}

	public static InDataSource ofFile(File zipFile) {
		return new InDataSource(null, zipFile);
	}

	/// Indicates whether this source is a local zip file; in this case the URL
	/// is `null`.
	public boolean isFile() {
		return zipFile != null;
	}
}
