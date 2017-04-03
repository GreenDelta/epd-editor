package app;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.LangString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.editors.BaseEditor;
import app.editors.Editors;
import app.editors.SimpleEditorInput;
import app.rcp.Icon;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import epd.model.RefStatus;
import epd.util.Strings;

public class StatusView extends BaseEditor {

	private static final HashMap<String, List<RefStatus>> _transfer = new HashMap<>(
			1);

	private String title;
	private List<RefStatus> stats;

	public static void open(String title, List<RefStatus> stats) {
		if (title == null || stats == null)
			return;
		ViewInput input = new ViewInput(title);
		_transfer.put(input.transferKey, stats);
		Editors.open(input, "status.view");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName(Strings.cut(input.getName(), 75));
		try {
			ViewInput vi = (ViewInput) input;
			title = vi.getName();
			stats = _transfer.remove(vi.transferKey);
		} catch (Exception e) {
			throw new PartInitException("Failed to open editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (PartInitException e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	private class Page extends FormPage {

		Page() {
			super(StatusView.this, "StatusViewPage", "#Status");
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			FormToolkit tk = mform.getToolkit();
			ScrolledForm form = UI.formHeader(mform, title);
			Composite body = UI.formBody(form, tk);
			TableViewer table = Tables.createViewer(body, M.Name, M.UUID,
					M.Version, "#Status");
			Tables.bindColumnWidths(table, 0.3, 0.2, 0.2, 0.3);
			table.setLabelProvider(new Label());
			table.setInput(stats);
			table.addDoubleClickListener(e -> {
				RefStatus rs = Viewers.getFirstSelected(table);
				if (rs != null && rs.ref != null)
					Editors.open(rs.ref);
			});
		}
	}

	private static class ViewInput extends SimpleEditorInput {

		private final String transferKey;

		public ViewInput(String title) {
			super(title);
			transferKey = UUID.randomUUID().toString();
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
	}

	private static class Label extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof RefStatus))
				return null;
			RefStatus rs = (RefStatus) obj;
			if (col == 0 && rs.ref != null)
				return Icon.img(rs.ref.type);
			if (col != 3)
				return null;
			switch (rs.value) {
			case RefStatus.CANCEL:
				return Icon.CANCELED.img();
			case RefStatus.ERROR:
				return Icon.ERROR.img();
			case RefStatus.INFO:
				return Icon.INFO.img();
			case RefStatus.OK:
				return Icon.OK.img();
			case RefStatus.WARNING:
				return Icon.WARNING.img();
			default:
				return null;
			}
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof RefStatus))
				return null;
			RefStatus rs = (RefStatus) obj;
			if (col == 3)
				return rs.message;
			if (rs.ref == null)
				return null;
			switch (col) {
			case 0:
				return LangString.getFirst(rs.ref.name, App.lang);
			case 1:
				return rs.ref.uuid;
			case 2:
				return rs.ref.version;
			default:
				return null;
			}
		}
	}
}
