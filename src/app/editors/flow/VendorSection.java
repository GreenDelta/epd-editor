package app.editors.flow;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.DataSetType;

import app.M;
import app.Tooltips;
import app.editors.RefLink;
import app.util.Controls;
import app.util.UI;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.epd.EpdMethodExtension;
import org.openlca.ilcd.util.Flows;

import java.util.function.BiConsumer;
import java.util.function.Function;

class VendorSection {

	private final FlowEditor editor;

	private VendorSection(FlowEditor editor) {
		this.editor = editor;
	}

	static void create(Composite body, FormToolkit tk, FlowEditor editor) {
		new VendorSection(editor).render(body, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk,
			M.VendorInformation, Tooltips.Flow_VendorInformation);
		vendorCheck(comp, tk);
		vendorRef(comp, tk);
		docRef(comp, tk);
	}

	private void vendorCheck(Composite comp, FormToolkit tk) {
		var check = UI.formCheckBox(comp, tk,
			M.IsVendorSpecific, Tooltips.Flow_IsVendorSpecific);
		check.setSelection(isVendorSpecific());
		Controls.onSelect(check, e -> {
			// clear to null if it is not vendor specific
			var b = check.getSelection() ? Boolean.TRUE : null;
			Flows.withInventoryMethod(editor.flow)
				.withEpdExtension()
				.withVendorSpecific(b);
			editor.setDirty();
		});
	}

	private boolean isVendorSpecific() {
		var m = Flows.getInventoryMethod(editor.flow);
		return m != null
			&& m.getEpdExtension() != null
			&& m.getEpdExtension().getVendorSpecific() != null
			&& m.getEpdExtension().getVendorSpecific();
	}

	private void vendorRef(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, M.Vendor, Tooltips.Flow_Vendor);
		onRef(
			new RefLink(comp, tk, DataSetType.CONTACT),
			EpdMethodExtension::getVendor,
			EpdMethodExtension::withVendor);
	}

	private void docRef(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk,
			M.Documentation, Tooltips.Flow_VendorDocumentation);
		onRef(
			new RefLink(comp, tk, DataSetType.SOURCE),
			EpdMethodExtension::getDocumentation,
			EpdMethodExtension::withDocumentation);
	}

	private void onRef(
		RefLink link,
		Function<EpdMethodExtension, Ref> get,
		BiConsumer<EpdMethodExtension, Ref> set
	) {
		var m = Flows.getInventoryMethod(editor.flow);
		if (m != null && m.getEpdExtension() != null) {
			link.setRef(get.apply(m.getEpdExtension()));
		}
		link.onChange(ref -> {
			var ext = Flows.withInventoryMethod(editor.flow)
				.withEpdExtension();
			set.accept(ext, ref);
			editor.setDirty();
		});
	}
}
