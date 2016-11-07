package app.editors;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import app.App;
import app.rcp.Icon;
import app.util.Controls;

public class RefText extends Composite {

	private Text text;
	private DataSetType type;
	private FormToolkit toolkit;
	private Ref ref;
	private Consumer<Ref> onChange;

	public RefText(Composite parent, FormToolkit tk, DataSetType type) {
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
		createAddButton();
		createTextField();
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
		Button btn = toolkit.createButton(this, "", SWT.PUSH);
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

	private void createTextField() {
		text = toolkit.createText(this, "", SWT.BORDER);
		text.setEditable(false);
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL,
				TableWrapData.FILL);
		layoutData.grabHorizontal = true;
		text.setLayoutData(layoutData);
		if (ref != null)
			text.setText(LangString.getFirst(ref.name, App.lang));
	}

	public void setRef(Ref ref) {
		this.ref = ref;
		if (text != null) {
			if (ref == null)
				text.setText("");
			else {
				String s = LangString.getFirst(ref.name, App.lang);
				text.setText(s == null ? "" : s);
			}
		}
		if (onChange != null)
			onChange.accept(ref);
	}

	public void onChange(Consumer<Ref> fn) {
		this.onChange = fn;
	}

}
