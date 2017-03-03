package app.editors.io;

import java.util.List;

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
import org.openlca.ilcd.io.SodaConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;

public class DownloadDialog extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final SodaConnection con;
	private final List<Ref> refs;

	private Page page;

	private DownloadDialog(SodaConnection con, List<Ref> refs) {
		this.refs = refs;
		this.con = con;
		setNeedsProgressMonitor(true);
	}

	public static int open(SodaConnection con, List<Ref> refs) {
		if (refs == null)
			return Window.CANCEL;
		DownloadDialog d = new DownloadDialog(con, refs);
		d.setWindowTitle("#Download data sets");
		WizardDialog dialog = new WizardDialog(UI.shell(), d);
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		SodaClient client = page.conCombo.makeClient();
		if (client == null)
			return false;
		try {
			getContainer().run(true, false, monitor -> {

			});
			return true;
		} catch (Exception e) {
			MsgBox.error("#Download failed", e.getMessage());
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private ConnectionCombo conCombo;
		private TableViewer table;
		private Button check;

		private Page() {
			super("DownloadDialogPage", "#Download data sets", null);
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
			conCombo.select(con);
			UI.filler(comp);
			check = new Button(comp, SWT.CHECK);
			check.setText("#Download dependencies");
			check.setSelection(true);
			createTable(parent);
		}

		private void createTable(Composite comp) {
			table = Tables.createViewer(comp, "#Data set", "UUID",
					"Version");
			table.setLabelProvider(new TableLabel());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			table.setInput(refs);
		}
	}
}
