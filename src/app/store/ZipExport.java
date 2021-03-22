package app.store;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import app.App;

public class ZipExport implements IRunnableWithProgress {

	private final File zipFile;

	private IProgressMonitor monitor;
	private FileSystem zip;

	public ZipExport(File zipFile) {
		this.zipFile = zipFile;
	}

	@Override
	public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
		if (zipFile == null)
			return;
		this.monitor = monitor;
		monitor.beginTask("#Package data folder", countFiles());
		try {
			String uriStr = zipFile.toURI().toASCIIString();
			URI uri = URI.create("jar:" + uriStr);
			Map<String, String> options = new HashMap<>();
			if (!zipFile.exists())
				options.put("create", "true");
			zip = FileSystems.newFileSystem(uri, options);
			packageData(zip);
			zip.close();
			monitor.done();
		} catch (Exception e) {
			throw new InvocationTargetException(e, e.getMessage());
		}
	}

	private int countFiles() {
		File rootDir = App.store().getRootFolder();
		if (!rootDir.exists())
			return 0;
		int c = 0;
		for (File folder : rootDir.listFiles()) {
			if (!folder.isDirectory())
				continue;
			for (File file : folder.listFiles()) {
				if (!file.isFile())
					continue;
				c++;
			}
		}
		return c;
	}

	private void packageData(FileSystem zip) throws Exception {
		String[] subDirs = {
			"contacts",
			"external_docs",
			"flowproperties",
			"flows",
			"lciamethods",
			"processes",
			"sources",
			"unitgroups"};
		for (String subDir : subDirs) {
			pack(subDir, subDir);
		}
		pack("classifications");
		pack("locations");
	}

	private void pack(String subDir, String... targetPath) throws Exception {
		File rootDir = App.store().getRootFolder();
		File dir = new File(rootDir, subDir);
		if (!dir.exists())
			return;
		monitor.subTask(subDir);
		Path parent = zip.getPath("ILCD", targetPath);
		if (!Files.exists(parent))
			Files.createDirectories(parent);
		String[] filePath = new String[targetPath.length + 1];
		System.arraycopy(targetPath, 0, filePath, 0, targetPath.length);
		for (File f : dir.listFiles()) {
			filePath[targetPath.length] = f.getName();
			Path target = zip.getPath("ILCD", filePath);
			Files.copy(f.toPath(), target,
				StandardCopyOption.REPLACE_EXISTING);
			monitor.worked(1);
		}
	}

}
