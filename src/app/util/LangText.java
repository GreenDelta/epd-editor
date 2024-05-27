package app.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.App;
import app.editors.IEditor;
import epd.util.Strings;

public class LangText {

	private final IEditor editor;
	private final FormToolkit tk;
	private final AtomicBoolean skipModifyEvents;

	private boolean multiLines;
	private String label;
	private String toolTip;
	private List<LangString> current;
	private Supplier<List<LangString>> onEdit;

	private Font italic;

	private LangText(IEditor editor, FormToolkit tk) {
		this.editor = editor;
		this.tk = tk;
		this.skipModifyEvents = new AtomicBoolean(false);
	}

	public static TextBuilder builder(IEditor editor, FormToolkit tk) {
		return new TextBuilder(editor, tk);
	}

	public LangText withMultiLines() {
		this.multiLines = true;
		return this;
	}

	public LangText withLabel(String label) {
		this.label = label;
		return this;
	}

	public LangText withToolTip(String toolTip) {
		this.toolTip = toolTip;
		return this;
	}

	public LangText val(List<LangString> current) {
		this.current = current;
		return this;
	}

	public LangText edit(Supplier<List<LangString>> s) {
		this.onEdit = s;
		return this;
	}

	public void draw(Composite comp) {
		UI.formLabel(comp, tk, label);
		var innerComp = tk.createComposite(comp);
		UI.gridData(innerComp, true, false);
		UI.innerGrid(innerComp, 2);
		int flags = multiLines
				? SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI
				: SWT.BORDER;
		var text = tk.createText(innerComp, "", flags);
		var grid = UI.gridData(text, true, false);
		if (multiLines) {
			grid.minimumHeight = 100;
			grid.heightHint = 100;
			grid.widthHint = 100;
		}
		setValue(text);

		text.addModifyListener($ -> {
			if (skipModifyEvents.get() || onEdit == null)
				return;
			applySystemFont(text);
			this.current = onEdit.get();
			var value = text.getText();
			if (Strings.notEmpty(value)) {
				LangString.set(current, value, App.lang());
			} else {
				LangString.remove(current, App.lang());
			}
			if (editor != null) {
				editor.setDirty();
			}
		});

		if (Strings.notEmpty(toolTip)) {
			text.setToolTipText(toolTip);
		}

		var link = tk.createImageHyperlink(innerComp, SWT.NONE);
		link.setForeground(Colors.linkBlue());
		link.setText(App.lang());
		var linkGrid = UI.gridData(link, false, false);
		linkGrid.verticalAlignment = SWT.TOP;
		linkGrid.verticalIndent = 2;

		Controls.onClick(link, $ -> {
			if (onEdit == null)
				return;
			var nextStrings = multiLines
					? LangTextDialog.openMultiLine(current)
					: LangTextDialog.open(current);
			if (nextStrings.isPresent()) {
				current = onEdit.get();
				current.clear();
				current.addAll(nextStrings.get());
				setValue(text);
				editor.setDirty();
			}
		});
	}

	private void setValue(Text text) {
		if (text == null)
			return;

		// skip modify events when the value is set
		// via non-ui events; e.g. when returning from
		// the editing dialog
		skipModifyEvents.set(true);
		var lang = App.lang();

		var val = LangString.get(current, lang);
		if (Strings.notEmpty(val)) {
			text.setText(val);
			applySystemFont(text);
		} else {
			var defaultVal = LangString.getDefault(current);
			if (Strings.notEmpty(defaultVal)) {
				text.setText(defaultVal);
				applyItalicFont(text);
			} else {
				applySystemFont(text);
			}
		}
		skipModifyEvents.set(false);
	}

	private void applySystemFont(Text text) {
		if (italic != null) {
			text.setFont(null);
		}
	}

	private void applyItalicFont(Text text) {
		if (italic == null) {
			italic = UI.italicFont(text);
			text.addDisposeListener(e -> {
				if (italic != null) {
					italic.dispose();
				}
			});
		}
		text.setFont(italic);
	}

	public static class TextBuilder {

		private final IEditor editor;
		private final FormToolkit tk;

		private TextBuilder(IEditor editor, FormToolkit tk) {
			this.editor = editor;
			this.tk = tk;
		}

		public LangText next(String label) {
			return next(label, null);
		}

		public LangText next(String label, String toolTip) {
			return new LangText(editor, tk)
					.withLabel(label)
					.withToolTip(toolTip);
		}

		public LangText nextMulti(String label) {
			return nextMulti(label, null);
		}

		public LangText nextMulti(String label, String toolTip) {
			return new LangText(editor, tk)
					.withMultiLines()
					.withLabel(label)
					.withToolTip(toolTip);
		}
	}

}
