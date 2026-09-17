package app.store;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.Categories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.StatusView;
import app.navi.NaviSync;
import app.util.MsgBox;
import epd.model.RefStatus;
import epd.refs.RefFetch;

/// Imports a single XML data set into the local data store: the type of the
/// data set is detected from the root element of the XML file and the file is
/// stored under the UUID of the data set in the folder of its type.
public class XmlImport implements IRunnableWithProgress {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File file;
	private final List<RefStatus> status = new ArrayList<>();

	public XmlImport(File file) {
		this.file = file;
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask(M.Import, 1);
		try {
			var data = Files.readAllBytes(file.toPath());

			// detect the type of the data set from the root element
			Ref ref;
			try (var is = new ByteArrayInputStream(data)) {
				ref = RefFetch.get(is).orElse(null);
			}
			if (ref == null || !ref.isValid()) {
				log.warn("no data set found in file {}", file);
				monitor.done();
				MsgBox.warn(M.Import, M.NoDataSetInFile);
				return;
			}

			// store the file under its UUID in the folder of its type
			List<Classification> classes;
			try (var is = new ByteArrayInputStream(data)) {
				classes = Categories.read(is);
			}
			var dir = App.store().getFolder(ref.getDataSetClass());
			var target = new File(dir, ref.getUUID() + ".xml");
			Files.write(target.toPath(), data,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
			RefTrees.remove(ref);
			App.index().add(ref, classes);
			status.add(RefStatus.ok(ref, M.Imported));

			App.getWorkspace().saveIndex();
			monitor.worked(1);
			monitor.done();
			App.runInUI("Refresh...", () -> new NaviSync(App.index()).run());
			StatusView.open(M.Import, status);
		} catch (Exception e) {
			monitor.done();
			log.error("failed to import data set from {}", file, e);
			throw new InvocationTargetException(e, e.getMessage());
		}
	}
}
