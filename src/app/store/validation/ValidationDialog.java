package app.store.validation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.StatusView;
import app.editors.RefTable;
import app.editors.RefTableLabel;
import app.store.RefDeps;
import app.util.Controls;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import epd.refs.Refs;

public class ValidationDialog extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Ref ref;
	private final Set<Ref> allRefs = new HashSet<>();

	private ValidationDialog(Ref ref) {
		this.ref = ref;
		addBaseRefs();
		setNeedsProgressMonitor(true);
	}

	public static int open(Ref ref) {
		if (ref == null)
			return Window.CANCEL;
		ValidationDialog d = new ValidationDialog(ref);
		WizardDialog dialog = new WizardDialog(UI.shell(), d);
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	/**
	 * Adds the references of the data sets to the list that need to be
	 * validated. This is typically just the reference that was selected when
	 * opening the validation dialog. However, for EPD data sets also the
	 * product flow is added as it is required to validate this product together
	 * with the EPD data set.
	 */
	private void addBaseRefs() {
		allRefs.clear();
		if (ref == null)
			return;
		allRefs.add(ref);
		if (ref.getType() != DataSetType.PROCESS)
			return;
		Process p = RefDeps.load(Process.class, ref);
		if (p == null)
			return;
		Exchange e = RefDeps.getRefExchange(p);
		if (e == null || e.getFlow() == null)
			return;
		allRefs.add(e.getFlow());
	}

	@Override
	public boolean performFinish() {
		if (App.settings().validationProfile == null) {
			MsgBox.error(M.NoValidationProfile, M.NoValidationProfile_Error);
			return false;
		}
		try {
			var v = new Validation(allRefs);
			getContainer().run(true, false, v);
			StatusView.open(M.Validation, v.getStatus());
			return true;
		} catch (Exception e) {
			MsgBox.error("#Error in validation", e.getMessage());
			log.error("validation failed", e);
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
			super("ValidationDialogPage",
				M.ValidateDataSet + ": " + App.header(ref.getName(), 50), null);
			setPageComplete(true);
		}

		@Override
		public void createControl(Composite root) {
			Composite comp = new Composite(root, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 1);
			createCheck(comp);
			createTable(comp);
		}

		private void createCheck(Composite comp) {
			Button check = new Button(comp, SWT.CHECK);
			check.setText(M.IncludeDependentDataSets);
			Controls.onSelect(check, e -> {
				if (check.getSelection()) {
					collectRefs();
				} else {
					addBaseRefs();
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
					var refs = Refs.allDependenciesOf(
							App.store(), ref, Refs::allEditableOf);
					allRefs.addAll(refs);
					App.runInUI("update table", () -> table.setInput(allRefs));
					monitor.done();
				});
			} catch (Exception e) {
				log.error("failed to collect references", e);
			}
		}
	}
}
