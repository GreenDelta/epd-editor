package app.store;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Sources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import app.App;
import app.M;
import app.editors.RefTable;
import app.editors.RefTableLabel;
import app.util.Colors;
import app.util.Controls;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import epd.refs.Refs;

/**
 * A dialog for exporting single data sets into ILCD zip packages.
 */
public class ExportDialog extends Wizard {

	private final Ref ref;
	private final List<Ref> all = new ArrayList<>();
	private Page page;

	private ExportDialog(Ref ref) {
		this.ref = ref;
		addBaseRefs();
		setNeedsProgressMonitor(true);
	}

	public static int open(Ref ref) {
		if (ref == null)
			return Window.CANCEL;
		ExportDialog d = new ExportDialog(ref);
		WizardDialog dialog = new WizardDialog(UI.shell(), d);
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	/**
	 * Adds the references of the data sets to the list that need to be
	 * exported. This is typically just the reference that was selected when
	 * opening the export dialog. However, for EPD data sets also the product
	 * flow is added as it is required to export this product together with the
	 * EPD data set.
	 */
	private void addBaseRefs() {
		all.clear();
		if (ref == null)
			return;
		all.add(ref);
		if (ref.getType() != DataSetType.PROCESS)
			return;
		Process p = RefDeps.load(Process.class, ref);
		if (p == null)
			return;
		Exchange e = RefDeps.getRefExchange(p);
		if (e == null || e.getFlow() == null)
			return;
		all.add(e.getFlow());
	}

	@Override
	public boolean performFinish() {
		if (Strings.isNullOrEmpty(page.filePath)) {
			MsgBox.error("No export file",
				"No export file was selected");
			return false;
		}
		try {
			File file = new File(page.filePath);
			if (file.exists()) {
				Files.delete(file.toPath());
			}
			ZipStore zip = new ZipStore(file);
			getContainer().run(true, false, new Exporter(zip));
			zip.close();
			return true;
		} catch (Exception e) {
			MsgBox.error("File export failed", "The file export failed with "
				+ "the following error: " + e.getMessage());
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to export file", e);
			return false;
		}

	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private TableViewer table;
		private String filePath;

		private Page() {
			super("ValidationDialogPage", M.ValidateDataSet + ": " +
				App.header(ref.getName(), 50), null);
			setPageComplete(true);
		}

		@Override
		public void createControl(Composite root) {
			Composite comp = new Composite(root, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 1);

			// the file selection component
			Composite browseComp = new Composite(comp, SWT.NONE);
			UI.gridData(browseComp, true, false);
			UI.innerGrid(browseComp, 3);
			Text text = UI.formText(browseComp, M.File);
			text.setEditable(false);
			text.setBackground(Colors.white());
			UI.gridData(text, true, false);
			Button browseButn = new Button(browseComp, SWT.NONE);
			browseButn.setText("Browse");
			Controls.onSelect(browseButn, e -> {
				String name = App.s(ref.getName()).replaceAll("\\W", "_");
				if (Strings.isNullOrEmpty(name)) {
					name = "dataset";
				}
				File file = FileChooser.save(name + ".zip", "*.zip");
				if (file == null)
					return;
				filePath = file.getAbsolutePath();
				text.setText(filePath);
			});

			// the dependency check
			Button depsCheck = new Button(comp, SWT.CHECK);
			depsCheck.setText(M.IncludeDependentDataSets);
			Controls.onSelect(depsCheck, e -> {
				if (depsCheck.getSelection()) {
					collectRefs();
				} else {
					addBaseRefs();
					table.setInput(all);
				}
			});

			// the table
			table = Tables.createViewer(comp,
				M.DataSet, M.UUID, M.DataSetVersion);
			table.setLabelProvider(new RefTableLabel());
			table.setComparator(RefTable.comparator());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			table.setInput(all);
		}

		private void collectRefs() {
			try {
				getContainer().run(true, false, monitor -> {
					monitor.beginTask(M.SearchDependentDataSets,
						IProgressMonitor.UNKNOWN);
					all.clear();
					var refs = Refs.allDependenciesOf(
							App.store(), ref, Refs::allEditableOf);
					all.addAll(refs);
					App.runInUI("update table", () -> table.setInput(all));
					monitor.done();
				});
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to collect references", e);
			}
		}
	}

	private class Exporter implements IRunnableWithProgress {

		private final ZipStore zip;

		private Exporter(ZipStore zip) {
			this.zip = zip;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			monitor.beginTask(M.Export, all.size() + 1);

			Set<String> handled = new HashSet<>();
			for (Ref ref : all) {
				monitor.worked(1);
				if (ref.getUUID() == null || handled.contains(ref.getUUID()))
					continue;
				handled.add(ref.getUUID());

				var ds = App.store().get(ref.getDataSetClass(), ref.getUUID());
				if (ds == null)
					continue;
				if (!(ds instanceof Source source)) {
					zip.put(ds);
					continue;
				}
				List<FileRef> fileRefs = Sources.getFileRefs(source);
				if (fileRefs == null || fileRefs.isEmpty()) {
					zip.put(source);
					continue;
				}
				File[] files = fileRefs.stream()
					.map(fileRef -> App.store()
						.getExternalDocument(fileRef))
					.filter(file -> file != null && file.exists())
					.toArray(File[]::new);
				zip.put(source, files);
			}
			monitor.done();
		}
	}

}
