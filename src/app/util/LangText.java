package app.util;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.App;
import app.editors.IEditor;
import epd.util.Strings;

public class LangText {

	private final IEditor editor;
	private final FormToolkit tk;

	private boolean multiLines;
	private String label;
	private String toolTip;
	private List<LangString> current;
	private Supplier<List<LangString>> onEdit;

	private LangText(IEditor editor, FormToolkit tk) {
		this.editor = editor;
		this.tk = tk;
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
		var text = tk.createText(innerComp, App.s(current), flags);
		var grid = UI.gridData(text, true, false);
		if (multiLines) {
			grid.minimumHeight = 100;
			grid.heightHint = 100;
			grid.widthHint = 100;
		}

		text.addModifyListener($ -> {
			if (onEdit == null)
				return;
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
				editor.setDirty();
			}
		});
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
