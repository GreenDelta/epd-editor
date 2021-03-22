package app.util;

import java.util.function.Consumer;

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

	public static void onSelect(Combo combo, Consumer<SelectionEvent> fn) {
		combo.addSelectionListener(onSelect(fn));
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
