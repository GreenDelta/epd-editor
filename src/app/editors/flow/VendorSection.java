package app.editors.flow;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;

import app.M;
import app.editors.RefLink;
import app.util.Controls;
import app.util.UI;

class VendorSection {

	private FlowEditor editor;
	private FormToolkit tk;

	private VendorSection(FlowEditor editor, FormToolkit tk) {
		this.editor = editor;
		this.tk = tk;
	}

	static void create(Composite body, FormToolkit tk, FlowEditor editor) {
		new VendorSection(editor, tk).render(body);
	}

	private void render(Composite body) {
		Composite comp = UI.formSection(body, tk, M.VendorInformation);
		Button check = UI.formCheckBox(comp, tk, M.IsVendorSpecific);
		check.setSelection(editor.product.vendorSpecific);
		Controls.onSelect(check, e -> {
			editor.product.vendorSpecific = check.getSelection();
			editor.setDirty();
		});
		vendorRef(comp);
		docRef(comp);
	}

	private void vendorRef(Composite comp) {
		UI.formLabel(comp, tk, M.Vendor);
		RefLink rt = new RefLink(comp, tk, DataSetType.CONTACT);
		rt.setRef(editor.product.vendor);
		rt.onChange(ref -> {
			editor.product.vendor = ref;
			editor.setDirty();
		});
	}

	private void docRef(Composite comp) {
		UI.formLabel(comp, tk, M.Documentation);
		RefLink rt = new RefLink(comp, tk, DataSetType.SOURCE);
		rt.setRef(editor.product.documentation);
		rt.onChange(ref -> {
			editor.product.documentation = ref;
			editor.setDirty();
		});
	}
}
