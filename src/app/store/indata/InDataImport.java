package app.store.indata;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.Xml;
import org.openlca.ilcd.util.DataSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.StatusView;
import app.navi.NaviSync;
import app.store.RefTrees;
import app.util.MsgBox;
import epd.model.RefStatus;
import epd.model.Version;

/// Imports the InData reference data (master data) from the zip file of the
/// InData GitHub repository or from a locally available zip file. A data set
/// is only imported when it is new or when the version in the archive is newer
/// than the version of the data set in the local store; otherwise it is
/// reported as existing. The result of the import is shown in a status view.
public class InDataImport implements IRunnableWithProgress {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final InDataSource source;
	private final List<RefStatus> status = new ArrayList<>();

	public InDataImport(InDataSource source) {
		this.source = source;
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask(M.ImportInDataRefData, IProgressMonitor.UNKNOWN);
		var zip = source.isFile()
			? source.zipFile()
			: download(monitor);
		try {
			if (zip != null) {
				importZip(zip, monitor);
			}
		} finally {
			if (zip != null && !source.isFile()) {
				delete(zip);
			}
			monitor.done();
		}
	}

	private void importZip(File zip, IProgressMonitor monitor) {
		monitor.subTask(zip.getName());
		try (var archive = InDataArchive.open(zip)) {
			var entries = archive.dataSets();
			if (entries.isEmpty()) {
				MsgBox.warn(M.ImportInDataRefData, M.NoDataSetInArchive);
				return;
			}
			for (var entry : entries) {
				if (monitor.isCanceled())
					break;
				monitor.subTask(entry.name());
				importDataSet(archive, entry);
				monitor.worked(1);
			}
		} catch (Exception e) {
			log.error("failed to import data sets from {}", zip, e);
			MsgBox.error(M.ImportInDataRefData, e.getMessage());
			return;
		}
		App.getWorkspace().saveIndex();
		App.runInUI("Refresh navigation ...",
			() -> new NaviSync(App.index()).run());
		if (!status.isEmpty()) {
			StatusView.open(M.ImportInDataRefData, status);
		}
	}

	private void importDataSet(InDataArchive archive,
			InDataArchive.Entry entry) {
		var ref = entry.ref();
		try {
			var type = ref.getDataSetClass();
			var oldVersion = storedVersion(type, ref);
			if (oldVersion != null
				&& !Version.isNewer(ref.getVersion(), oldVersion)) {
				status.add(RefStatus.info(ref, M.AlreadyExists));
				return;
			}
			var data = archive.read(entry);
			var ds = Xml.read(type, data);
			if (ds == null) {
				status.add(RefStatus.error(ref, "Failed to read data set"));
				return;
			}
			App.store().put(ds);
			if (oldVersion != null) {
				App.index().remove(App.index().find(ref));
			}
			App.index().add(ds);
			RefTrees.cache(ds);
			status.add(RefStatus.ok(ref,
				oldVersion == null ? M.Imported : M.Updated));
		} catch (Exception e) {
			log.error("failed to import data set {}", entry.name(), e);
			status.add(RefStatus.error(ref, e.getMessage()));
		}
	}

	/// Returns the version of the data set with the same UUID that is already
	/// stored in the local store or `null` when there is no such data set.
	private String storedVersion(Class<? extends IDataSet> type, Ref ref) {
		var stored = App.index().find(ref);
		if (stored != null)
			return stored.getVersion();
		if (!App.store().contains(type, ref.getUUID()))
			return null;
		var ds = App.store().get(type, ref.getUUID());
		return ds == null ? null : DataSets.getVersion(ds);
	}

	/// Downloads the zip file from the URL of the source. Returns `null` when
	/// the download failed.
	private File download(IProgressMonitor monitor) {
		File target = null;
		monitor.subTask(source.url());
		try {
			target = File.createTempFile("indata-master-data-", ".zip");
			var client = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
			try (client) {
				var request = HttpRequest.newBuilder()
					.uri(URI.create(source.url()))
					.header("Accept", "application/zip")
					.GET()
					.build();
				var handler = HttpResponse.BodyHandlers
					.ofFile(target.toPath());
				var response = client.send(request, handler);
				if (response.statusCode() == 200)
					return target;
				log.error("download failed with status {} for {}",
					response.statusCode(), source.url());
				MsgBox.error(M.DownloadFailed, source.url()
					+ " (" + response.statusCode() + ")");
			}
		} catch (Exception e) {
			log.error("failed to download {}", source.url(), e);
			MsgBox.error(M.DownloadFailed, source.url());
		}
		delete(target);
		return null;
	}

	private void delete(File file) {
		if (file == null)
			return;
		try {
			Files.deleteIfExists(file.toPath());
		} catch (Exception e) {
			log.error("failed to delete file {}", file, e);
		}
	}
}
