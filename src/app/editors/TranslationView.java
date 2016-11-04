package app.editors;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.openlca.ilcd.commons.LangString;

import epd.io.EpdStore;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class TranslationView extends ViewPart implements ISelectionListener {

	private AnchorPane pane;

	@Override
	public void createPartControl(Composite parent) {
		FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
		canvas.setLayout(new FillLayout());
		pane = new AnchorPane();
		Scene scene = new Scene(pane);
		canvas.setScene(scene);
		ISelectionService service = getSite().getWorkbenchWindow()
				.getSelectionService();
		if (service != null)
			service.addSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof Selection))
			return;
		pane.getChildren().clear();
		Selection s = (Selection) selection;
		if (s.isEmpty())
			return;
		Label label = new Label();
		label.setStyle("-fx-font-size: 14;-fx-font-weight: bold");
		pane.getChildren().add(label);
		AnchorPane.setLeftAnchor(label, 15d);
		AnchorPane.setTopAnchor(label, 15d);
		label.setText(s.title);

		int i = 1;
		for (LangString ls : s.strings) {
			Label l = new Label();
			AnchorPane.setLeftAnchor(l, 15d);
			AnchorPane.setTopAnchor(l, 18d + i * 35);
			l.setText("@" + ls.lang);
			pane.getChildren().add(l);
			if (Objects.equals(ls.lang, EpdStore.lang))
				l.setStyle("-fx-font-weight: bold");

			TextField text = new TextField();
			text.setEditable(false);
			AnchorPane.setLeftAnchor(text, 60d);
			AnchorPane.setTopAnchor(text, 15d + i * 35);
			AnchorPane.setRightAnchor(text, 15d);
			if (ls.value != null)
				text.setText(ls.value);
			pane.getChildren().add(text);

			i++;
		}
	}

	@Override
	public void dispose() {
		ISelectionService service = getSite().getWorkbenchWindow()
				.getSelectionService();
		if (service != null)
			service.removeSelectionListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	public static void register(WorkbenchPart part, String title, Text text,
			List<LangString> strings) {
		if (text == null)
			return;
		text.addModifyListener(e -> {
			Selection.set(part, title, strings);
		});
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
