package app.editors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.RefTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Controls;
import app.util.Tables;
import app.util.UI;

public class UploadDialog extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final Ref ref;
	private final List<Ref> allRefs = new ArrayList<>();
	private Page page;

	private UploadDialog(Ref ref) {
		this.ref = ref;
		allRefs.add(ref);
		setNeedsProgressMonitor(true);
	}

	public static int open(Ref dataSet) {
		if (dataSet == null)
			return Window.CANCEL;
		UploadDialog d = new UploadDialog(dataSet);
		d.setWindowTitle(M.UploadDataSet);
		WizardDialog dialog = new WizardDialog(UI.shell(), d);
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private TableViewer table;

		private Page() {
			super("UploadDialogPage", M.UploadDataSet + ": " +
					App.header(ref.name, 75), null);
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
			Combo combo = UI.formCombo(comp, "#Connection");
			UI.filler(comp);
			Button check = new Button(comp, SWT.CHECK);
			check.setText("#Synchronize dependencies");
			Controls.onSelect(check, e -> {
				collectRefs();
			});
			table = Tables.createViewer(parent, "#Data set", "UUID",
					"Version");
			table.setLabelProvider(new TableLabel());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			table.setInput(allRefs);
		}

		private void collectRefs() {
			try {
				getContainer().run(true, false, monitor -> {
					monitor.beginTask("#Collect references:",
							IProgressMonitor.UNKNOWN);
					allRefs.clear();
					ArrayDeque<Ref> queue = new ArrayDeque<>();
					queue.push(ref);
					while (!queue.isEmpty()) {
						Ref next = queue.poll();
						allRefs.add(next);
						monitor.subTask(App.header(next.name, 75));
						collectNext(next, queue);
					}
					App.runInUI("update table", () -> table.setInput(allRefs));
					monitor.done();
				});
			} catch (Exception e) {
				log.error("failed to collect references", e);
			}
		}

		private void collectNext(Ref next, ArrayDeque<Ref> queue) {
			try {
				IDataSet ds = App.store.get(next.getDataSetClass(), next.uuid);
				if (ds == null) {
					log.warn("could not get data set for {}", next);
					return;
				}
				for (Ref dep : RefTree.create(ds).getRefs()) {
					if (allRefs.contains(dep) || queue.contains(dep))
						continue;
					queue.add(dep);
				}
			} catch (Exception e) {
				log.error("failed to get dependencies for {}", next, e);
			}
		}
	}

	private class TableLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (col != 0 || !(obj instanceof Ref))
				return null;
			Ref ref = (Ref) obj;
			return Icon.img(ref.type);
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Ref))
				return null;
			Ref ref = (Ref) obj;
			switch (col) {
			case 0:
				return LangString.getFirst(ref.name, App.lang);
			case 1:
				return ref.uuid;
			case 2:
				return ref.version;
			default:
				return null;
			}
		}
	}
}
