package app.util;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class Controls {

	private Controls() {
	}

	/// Adds the given selection handler to the given combo.
	public static void onSelect(Combo combo, Consumer<SelectionEvent> fn) {
		if (combo == null || fn == null)
			return;
		combo.addSelectionListener(onSelect(e -> {
			fn.accept(e);
			// On GTK the pop-up list of a combo can take the focus so that it falls
			// back to the first control of the page afterwards, which scrolls a
			// scrolled form to its top; thus we set the focus back to the combo here.
			if (!combo.isDisposed()) {
				combo.setFocus();
			}
		}));
	}

	/// Adds the given selection handler to the given combo viewer.
	public static void onSelect(
		ComboViewer viewer, Consumer<SelectionChangedEvent> fn
	) {
		if (viewer == null || fn == null)
			return;
		viewer.addSelectionChangedListener(e -> {
			fn.accept(e);
			var control = viewer.getControl();
			// On GTK the pop-up list of a combo can take the focus so that it falls
			// back to the first control of the page afterwards, which scrolls a
			// scrolled form to its top; thus we set the focus back to the combo here.
			if (control != null && !control.isDisposed()) {
				control.setFocus();
			}
		});
	}

	public static void onSelect(Button button, Consumer<SelectionEvent> fn) {
		button.addSelectionListener(onSelect(fn));
	}

	public static void onSelect(MenuItem item, Consumer<SelectionEvent> fn) {
		item.addSelectionListener(onSelect(fn));
	}

	public static void onSelect(Scale scale, Consumer<SelectionEvent> fn) {
		scale.addSelectionListener(onSelect(fn));
	}

	public static void onSelect(Link link, Consumer<SelectionEvent> fn) {
		link.addSelectionListener(onSelect(fn));
	}

	public static void onSelect(Spinner spinner, Consumer<SelectionEvent> fn) {
		spinner.addSelectionListener(onSelect(fn));
	}

	public static SelectionListener onSelect(Consumer<SelectionEvent> fn) {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fn.accept(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				fn.accept(e);
			}
		};
	}

	public static void onClick(Hyperlink link, Consumer<HyperlinkEvent> fn) {
		if (link == null || fn == null)
			return;
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				fn.accept(e);
			}
		});
	}

	public static void onClick(Label label, Consumer<MouseEvent> fn) {
		if (label == null || fn == null)
			return;
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				fn.accept(e);
			}
		});
	}
}
