package app.editors.refs;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.M;
import app.editors.Editors;
import app.rcp.Icon;
import app.util.Colors;
import app.util.Controls;
import app.util.UI;

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
		Controls.onClick(link, _ -> {
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
		var btn = toolkit.createButton(this, "", SWT.PUSH);
		btn.setToolTipText(M.SelectDataSet);
		btn.setImage(Icon.img(type));
		Controls.onSelect(btn, _ -> {
			Ref ref = RefSelectionDialog.select(type);
			if (ref != null)
				setRef(ref);
		});
	}

	private void createRemoveLink() {
		var link = toolkit.createImageHyperlink(this, SWT.BOTTOM);
		link.setToolTipText(M.Remove);
		link.setHoverImage(Icon.DELETE.img());
		link.setImage(Icon.DELETE_DIS.img());
		Controls.onClick(link, _ -> setRef(null));
	}

	public void setRef(Ref ref) {
		this.ref = ref;
		setLinkText();
		this.pack();
		if (onChange != null) {
			onChange.accept(ref);
		}
	}

	public void onChange(Consumer<Ref> fn) {
		this.onChange = fn;
	}

	private void setLinkText() {
		if (link == null)
			return;
		String t = M.None;
		if (ref != null) {
			var s = App.s(ref.getName());
			if (Strings.isNotBlank(s)) {
				t = s;
			}
		}
		t = Strings.cutEnd(t, 120);
		link.setText(t);
	}

}
