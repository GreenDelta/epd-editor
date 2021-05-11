package app.editors.io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.editors.RefTable;
import app.store.EpdProfiles;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.util.DependencyTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.StatusView;
import app.editors.RefTableLabel;
import app.util.Controls;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import epd.model.RefStatus;
import epd.util.ExtensionRefs;

public class UploadDialog extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Ref ref;
	private final Set<Ref> allRefs = new HashSet<>();

	private ConnectionCombo conCombo;

	private UploadDialog(Ref ref) {
		this.ref = ref;
		allRefs.add(ref);
		setNeedsProgressMonitor(true);
	}

	public static int open(Ref ref) {
		if (ref == null)
			return Window.CANCEL;
		UploadDialog d = new UploadDialog(ref);
		d.setWindowTitle(M.UploadDataSet);
		WizardDialog dialog = new WizardDialog(UI.shell(), d);
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		SodaClient client = conCombo.makeClient();
		if (client == null)
			return false;
		try {
			Upload upload = new Upload(client);
			getContainer().run(true, false, monitor -> {
				monitor.beginTask("Upload", allRefs.size());
				List<RefStatus> stats = new ArrayList<>();
				for (Ref ref : allRefs) {
					monitor.subTask(App.header(ref.name, 50));
					stats.add(upload.next(ref));
					monitor.worked(1);
				}
				monitor.done();
				StatusView.open("#Upload result", stats);
			});
			return true;
		} catch (Exception e) {
			MsgBox.error("#Upload failed", e.getMessage());
			return false;
		}
	}

	@Override
	public void addPages() {
		addPage(new Page());
	}

	private class Page extends WizardPage {

		private TableViewer table;

		private Page() {
			super("UploadDialogPage",
				M.UploadDataSet + ": " + App.header(ref.name, 50), null);
			setPageComplete(true);
		}

		@Override
		public void createControl(Composite root) {
			Composite parent = new Composite(root, SWT.NONE);
			setControl(parent);
			UI.gridLayout(parent, 1);
			Composite comp = new Composite(parent, SWT.NONE);
			UI.innerGrid(comp, 2).verticalSpacing = 10;
			UI.gridData(comp, true, false);
			conCombo = ConnectionCombo.create(comp);
			UI.filler(comp);
			createCheck(comp);
			createTable(parent);
			App.runInUI(M.SearchDependentDataSets, this::collectRefs);
		}

		private void createCheck(Composite comp) {
			Button check = new Button(comp, SWT.CHECK);
			check.setText(M.SynchronizeDependentDataSets);
			check.setSelection(true);
			Controls.onSelect(check, e -> {
				if (check.getSelection()) {
					collectRefs();
				} else {
					allRefs.clear();
					allRefs.add(ref);
					table.setInput(allRefs);
				}
			});
		}

		private void createTable(Composite parent) {
			table = Tables.createViewer(parent, M.DataSet, M.UUID,
				M.DataSetVersion);
			table.setLabelProvider(new RefTableLabel());
			table.setComparator(RefTable.comparator());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			table.setInput(allRefs);
		}

		private void collectRefs() {
			try {
				getContainer().run(true, false, monitor -> {
					monitor.beginTask(
						M.SearchDependentDataSets,
						IProgressMonitor.UNKNOWN);
					allRefs.clear();

					DependencyTraversal.of(App.store(), ref)
						.filter(r -> !EpdProfiles.isProfileRef(r))
						.forEach(ds -> {
							Ref next = Ref.of(ds);
							monitor.subTask(App.header(next.name, 75));
							allRefs.add(next);
							ExtensionRefs.of(ds)
								.stream()
								.filter(r -> !EpdProfiles.isProfileRef(r))
								.forEach(allRefs::add);
						});

					App.runInUI("update table", () -> table.setInput(allRefs));
					monitor.done();
				});
			} catch (Exception e) {
				log.error("failed to collect references", e);
			}
		}


	}
}
