package app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.Editors;
import app.editors.RefTableLabel;
import app.editors.SimpleEditorInput;
import app.rcp.Icon;
import app.util.Controls;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

public class SearchPage extends FormEditor {

	public static void open() {
		Editors.open(new SimpleEditorInput("search.page"), "app.SearchPage");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add search page");
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName(M.Search);
	}

	@Override
	public void doSave(IProgressMonitor m) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private class Page extends FormPage {

		private Text text;
		private TableViewer table;

		Page() {
			super(SearchPage.this, "SearchPage", M.Search);
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			FormToolkit tk = mform.getToolkit();
			Composite body = UI.formBody(mform.getForm(), tk);
			createControls(body, tk);
			Composite comp = UI.formComposite(body, tk);
			UI.gridData(comp, true, true);
			table = Tables.createViewer(comp, M.Name, M.UUID, M.Version);
			table.setLabelProvider(new RefTableLabel());
			Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
			Tables.onDoubleClick(table, e -> {
				Ref ref = Viewers.getFirstSelected(table);
				if (ref != null) {
					Editors.open(ref);
				}
			});
		}

		private void createControls(Composite body, FormToolkit tk) {
			Composite comp = UI.formComposite(body, tk);
			UI.gridData(comp, true, false);
			text = tk.createText(comp, null, SWT.BORDER);
			UI.gridData(text, true, false).heightHint = 20;
			text.addTraverseListener(e -> {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					doIt();
				}
			});
			Button b = new Button(comp, SWT.NONE);
			b.setImage(Icon.SEARCH.img());
			b.setToolTipText(M.Search);
			Controls.onSelect(b, e -> doIt());
		}

		private void doIt() {
			String term = text.getText().trim().toLowerCase();
			Set<Ref> refs = App.index.getRefs();
			List<Ref> filtered = new ArrayList<>();
			for (Ref ref : refs) {
				String name = LangString.getFirst(ref.name, App.lang());
				if (name == null)
					continue;
				name = name.toLowerCase();
				if (name.contains(term)) {
					filtered.add(ref);
				}
			}
			filtered.sort((r1, r2) -> {
				String n1 = LangString.getFirst(r1.name, App.lang())
						.toLowerCase();
				String n2 = LangString.getFirst(r2.name, App.lang())
						.toLowerCase();
				return n1.indexOf(term) - n2.indexOf(term);

			});
			table.setInput(filtered);
		}
	}

}
