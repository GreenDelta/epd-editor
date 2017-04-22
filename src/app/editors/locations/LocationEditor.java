package app.editors.locations;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.lists.LocationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.store.Locations;
import app.util.Tables;
import app.util.UI;

public class LocationEditor extends BaseEditor {

	private File file;
	private LocationList list;

	public static void open(File file) {
		if (file == null || !file.exists())
			return;
		EditorInput input = new EditorInput(file);
		Editors.open(input, "location.editor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			EditorInput ei = (EditorInput) input;
			file = ei.file;
			list = Locations.getList(ei.file);
			setPartName("#Locations - " + ei.file.getName());
		} catch (Exception e) {
			throw new PartInitException("Failed to open editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	private static class EditorInput extends SimpleEditorInput {

		final File file;

		EditorInput(File file) {
			super("#Locations - " + file.getName());
			this.file = file;
		}
	}

	private class Page extends FormPage {

		private Page() {
			super(LocationEditor.this, "LocationPage",
					"#Locations");
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			ScrolledForm form = UI.formHeader(mform, "#Locations - "
					+ file.getName());
			FormToolkit tk = mform.getToolkit();
			Composite body = UI.formBody(form, tk);
			TableViewer table = Tables.createViewer(body, "#Code", "#Name");
			Tables.bindColumnWidths(table, 0.4, 0.6);
			table.setLabelProvider(new TableLable());
			form.reflow(true);
			table.setInput(list.locations);
		}
	}

}
