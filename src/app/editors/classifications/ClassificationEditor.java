package app.editors.classifications;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.lists.CategorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.rcp.Icon;
import app.store.CategorySystems;
import app.util.UI;

public class ClassificationEditor extends BaseEditor {

	private CategorySystem system;

	public static void open(File file) {
		if (file == null || !file.exists())
			return;
		EditorInput input = new EditorInput(file);
		Editors.open(input, "classification.editor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			EditorInput ei = (EditorInput) input;
			system = CategorySystems.get(ei.file);
			setPartName("#Classifications - " + system.name);
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
			super("#Classifications - " + file.getName());
			this.file = file;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Icon.FOLDER.des();
		}
	}

	private class Page extends FormPage {

		private Page() {
			super(ClassificationEditor.this, "ClassificationPage",
					"#Classifications");
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			ScrolledForm form = UI.formHeader(mform, "# Classifications - "
					+ system.name);
			FormToolkit tk = mform.getToolkit();
			Composite body = UI.formBody(form, tk);
			body.setLayout(new FillLayout());
			TreeViewer tree = new TreeViewer(body);
			tree.setContentProvider(new TreeContent());
			tree.setLabelProvider(new TreeLabel());
			form.reflow(true);
			tree.setInput(system);
			tree.expandToLevel(2);
		}
	}
}
