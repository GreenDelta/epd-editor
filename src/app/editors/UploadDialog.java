package app.editors;

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
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Tables;
import app.util.UI;
import epd.util.Strings;

public class UploadDialog extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final Ref ref;
	private Page page;

	private UploadDialog(Ref ref) {
		this.ref = ref;
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
					Strings.cut(App.s(ref.name), 75),
					null);
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
			table = Tables.createViewer(parent, "#Data set", "UUID",
					"Version");
			table.setLabelProvider(new TableLabel());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			table.setInput(new Ref[] { ref });
		}
	}

	private class TableLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Ref))
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
				return App.s(ref.name);
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
