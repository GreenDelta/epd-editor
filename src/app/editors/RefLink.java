package app.editors;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Colors;
import app.util.Controls;
import app.util.UI;
import epd.util.Strings;

public class RefLink extends Composite {

	private ImageHyperlink link;
	private final DataSetType type;
	private final FormToolkit toolkit;
	private Ref ref;
	private Consumer<Ref> onChange;

	public RefLink(Composite parent, FormToolkit tk, DataSetType type) {
		super(parent, SWT.FILL);
		this.type = type;
		this.toolkit = tk;
		createContent();
	}

	private void createContent() {
		toolkit.adapt(this);
		UI.innerGrid(this, 3);
		createAddButton();
		link = toolkit.createImageHyperlink(this, SWT.TOP);
		link.setForeground(Colors.linkBlue());
		Controls.onClick(link, e -> {
			if (ref != null)
				Editors.open(ref);
			else {
				Ref ref = RefSelectionDialog.select(type);
				if (ref != null)
					setRef(ref);
			}
		});
		setLinkText();
		createRemoveLink();
	}

	private void createAddButton() {
		Button btn = toolkit.createButton(this, "", SWT.PUSH);
		btn.setToolTipText(M.SelectDataSet);
		btn.setImage(Icon.img(type));
		Controls.onSelect(btn, e -> {
			Ref ref = RefSelectionDialog.select(type);
			if (ref != null)
				setRef(ref);
		});
	}

	private void createRemoveLink() {
		ImageHyperlink l = toolkit.createImageHyperlink(this, SWT.BOTTOM);
		l.setToolTipText(M.Remove);
		l.setHoverImage(Icon.DELETE.img());
		l.setImage(Icon.DELETE_DIS.img());
		Controls.onClick(l, e -> setRef(null));
	}

	public void setRef(Ref ref) {
		this.ref = ref;
		setLinkText();
		this.pack();
		if (onChange != null)
			onChange.accept(ref);
	}

	public void onChange(Consumer<Ref> fn) {
		this.onChange = fn;
	}

	private void setLinkText() {
		if (link == null)
			return;
		String t = M.None;
		if (ref != null) {
			String s = LangString.getFirst(ref.getName(), App.lang());
			if (s != null) {
				t = s;
			}
		}
		t = Strings.cut(t, 120);
		link.setText(t);
	}

}
