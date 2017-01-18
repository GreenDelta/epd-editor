package app.editors.connection;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.descriptors.Descriptor;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Controls;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import epd.util.Strings;

class DataSection {

	private final SodaConnection con;
	private TableViewer table;

	DataSection(SodaConnection con) {
		this.con = con;
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "#Data");
		UI.gridData(section, true, true);
		Composite parent = UI.sectionClient(section, tk);
		UI.gridLayout(parent, 1);
		Composite comp = tk.createComposite(parent);
		UI.gridData(comp, true, false);
		UI.innerGrid(comp, 3);
		TypeCombo typeCombo = TypeCombo.create(comp, tk);
		Text searchText = tk.createText(comp, "", SWT.BORDER);
		UI.gridData(searchText, false, false).widthHint = 350;
		Button button = tk.createButton(comp, M.Search, SWT.NONE);
		Controls.onSelect(button, e -> {
			runSearch(typeCombo.selectedType, searchText.getText());
		});
		table = Tables.createViewer(parent, M.Name, M.UUID, M.Version,
				M.Comment);
		Tables.bindColumnWidths(table, 0.3, 0.2, 0.2, 0.3);
		table.setLabelProvider(new Label());
	}

	private void runSearch(DataSetType type, String name) {
		Class<? extends IDataSet> cs = getClass(type);
		if (cs == null)
			return;
		try (SodaClient client = new SodaClient(con)) {
			client.connect();
			DescriptorList list = client.search(cs, name);
			table.setInput(list.descriptors);
		} catch (Exception e) {
			MsgBox.error("#Search failed", e.getMessage());
		}
	}

	private Class<? extends IDataSet> getClass(DataSetType type) {
		if (type == null)
			return null;
		switch (type) {
		case CONTACT:
			return Contact.class;
		case FLOW:
			return Flow.class;
		case FLOW_PROPERTY:
			return FlowProperty.class;
		case LCIA_METHOD:
			return LCIAMethod.class;
		case PROCESS:
			return Process.class;
		case SOURCE:
			return Source.class;
		case UNIT_GROUP:
			return UnitGroup.class;
		default:
			return null;
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof Descriptor))
				return null;
			if (col != 0)
				return null;
			Descriptor d = (Descriptor) obj;
			return Icon.img(d.toRef().type);
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Descriptor))
				return null;
			Descriptor d = (Descriptor) obj;
			switch (col) {
			case 0:
				return LangString.getFirst(d.name, App.lang);
			case 1:
				return d.uuid;
			case 2:
				return d.version;
			case 3:
				String val = LangString.getFirst(d.comment, App.lang);
				return Strings.cut(val, 75);
			default:
				return null;
			}
		}
	}
}
