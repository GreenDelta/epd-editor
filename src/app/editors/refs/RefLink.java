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

	private final DataSetType type;
	private final FormToolkit toolkit;
	private final boolean readOnly;
	private ImageHyperlink link;
	private Ref ref;
	private Consumer<Ref> onChange;

	public RefLink(Composite parent, FormToolkit tk, DataSetType type) {
		this(parent, tk, type, false);
	}

	/// Creates a link that only displays the reference. The controls for
	/// selecting and removing a reference are not created.
	public static RefLink readOnly(
		Composite parent, FormToolkit tk, DataSetType type
	) {
		return new RefLink(parent, tk, type, true);
	}

	private RefLink(
		Composite parent, FormToolkit tk, DataSetType type, boolean readOnly
	) {
		super(parent, SWT.FILL);
		this.type = type;
		this.toolkit = tk;
		this.readOnly = readOnly;
		createContent();
	}

	private void createContent() {
		toolkit.adapt(this);
		UI.innerGrid(this, readOnly ? 1 : 3);
		if (!readOnly) {
			createAddButton();
		}
		link = createLink();
		setLinkText();
		if (!readOnly) {
			createRemoveLink();
		}
	}

	private ImageHyperlink createLink() {
		var hyperlink = toolkit.createImageHyperlink(this, SWT.TOP);
		hyperlink.setForeground(Colors.linkBlue());
		Controls.onClick(hyperlink, _ -> {
			if (ref != null) {
				Editors.open(ref);
			} else if (!readOnly) {
				var selected = RefSelectionDialog.select(type);
				if (selected != null) {
					setRef(selected);
				}
			}
		});
		return hyperlink;
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
