package app.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.openlca.ilcd.commons.LangString;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.util.Colors;
import app.util.UI;
import epd.util.Strings;

public class TranslationView extends ViewPart implements ISelectionListener {

	private FormToolkit toolkit;
	private ScrolledForm form;
	private Composite composite;
	private final List<Control> controls = new ArrayList<>();

	public static void open() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView("TranslationView");
		} catch (Exception e) {
			LoggerFactory.getLogger(TranslationView.class)
					.error("failed to open translations", e);
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento)
			throws PartInitException {
		super.init(site, memento);
		setPartName(M.Translations);
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		parent.addDisposeListener(e -> toolkit.dispose());
		form = toolkit.createScrolledForm(parent);
		form.setText(M.Translation_NoContentsYet);
		form.getForm().setForeground(Colors.get(70, 70, 70));
		composite = form.getBody();
		UI.gridLayout(composite, 2);
		var service = getSite()
				.getWorkbenchWindow()
				.getSelectionService();
		if (service != null) {
			service.addSelectionListener(this);
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof Selection))
			return;
		controls.forEach(Widget::dispose);
		controls.clear();
		var s = (Selection) selection;
		if (s.isEmpty()) {
			form.setText(M.Translation_NoContentsYet);
			return;
		}
		if (s.title != null)
			form.setText(s.title);
		s.strings.sort((s1, s2) -> Strings.compare(s1.lang, s2.lang));
		for (LangString ls : s.strings) {
			String lang = ls.lang == null ? M.None : ls.lang;
			Label label = UI.formLabel(composite, toolkit, lang + ":");
			controls.add(label);
			if (Objects.equals(ls.lang, App.lang()))
				label.setFont(UI.boldFont());
			Text text = UI.formMultiText(composite, toolkit);
			controls.add(text);
			text.setEditable(false);
			if (ls.value != null)
				text.setText(ls.value);
		}
		form.reflow(true);
	}

	@Override
	public void dispose() {
		var service = getSite()
				.getWorkbenchWindow()
				.getSelectionService();
		if (service != null)
			service.removeSelectionListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	public static void register(
			WorkbenchPart part, String title, Text text,
			List<LangString> strings) {
		if (text == null)
			return;
		text.addModifyListener(e -> Selection.set(part, title, strings));
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Selection.set(part, title, strings);
			}

			@Override
			public void focusLost(FocusEvent e) {
				Selection.setEmpty(part);
			}
		});
	}

	private static class Selection implements ISelection {
		final String title;
		final List<LangString> strings;

		Selection(String title, List<LangString> strings) {
			this.title = title;
			this.strings = strings;
		}

		@Override
		public boolean isEmpty() {
			return title == null || strings == null || strings.isEmpty();
		}

		static void set(WorkbenchPart part, String title,
				List<LangString> strings) {
			if (part == null)
				return;
			Selection s = new Selection(title, strings);
			part.getSite().getSelectionProvider().setSelection(s);
		}

		static void setEmpty(WorkbenchPart part) {
			Selection s = new Selection(null, null);
			part.getSite().getSelectionProvider().setSelection(s);
		}
	}

}
