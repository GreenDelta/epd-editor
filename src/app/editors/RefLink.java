package app.editors;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;
import app.util.Colors;
import app.util.Controls;
import epd.util.Strings;

public class RefLink extends Composite {

	private ImageHyperlink link;
	private DataSetType type;
	private FormToolkit toolkit;
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
		TableWrapLayout layout = createLayout();
		setLayout(layout);
		// order of the method calls is important (fills from left to right)
		createLink();
		createAddButton();
		createRemoveButton();
	}

	private TableWrapLayout createLayout() {
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 3;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		return layout;
	}

	private void createAddButton() {
		Button btn = toolkit.createButton(this, "#Select", SWT.PUSH);
		btn.setToolTipText("#Select data set");
		btn.setLayoutData(new TableWrapData());
		btn.setImage(Icon.img(type));
		Controls.onSelect(btn, e -> {
			Ref ref = RefSelectionDialog.select(type);
			if (ref != null)
				setRef(ref);
		});
	}

	private void createRemoveButton() {
		Button btn = toolkit.createButton(this, "", SWT.PUSH);
		btn.setLayoutData(new TableWrapData());
		btn.setImage(Icon.DELETE.img());
		btn.setToolTipText("#Remove data set link");
		Controls.onSelect(btn, e -> {
			setRef(null);
		});
	}

	private void createLink() {
		link = toolkit.createImageHyperlink(this, SWT.NONE);
		link.setForeground(Colors.linkBlue());
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL,
				TableWrapData.FILL);
		layoutData.grabHorizontal = true;
		link.setLayoutData(layoutData);
		setLinkText();
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
		String t = "#none";
		if (ref != null) {
			String s = LangString.getFirst(ref.name, App.lang);
			if (s != null)
				t = s;
		}
		t = Strings.cut(t, 120);
		link.setText(t);
	}

}
