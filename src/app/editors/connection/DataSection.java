package app.editors.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
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
import app.editors.io.DownloadDialog;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Controls;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

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
		table.setLabelProvider(new TableLabel());
		bindDownload();
	}

	private void runSearch(DataSetType type, String name) {
		Class<? extends IDataSet> clazz = getClass(type);
		if (clazz == null)
			return;
		String[] error = new String[1];
		List<Descriptor> result = new ArrayList<>();
		App.run("#Search online", () -> {
			try (SodaClient client = new SodaClient(con)) {
				client.connect();
				DescriptorList list = client.search(clazz, name);
				result.addAll(list.descriptors);
			} catch (Exception e) {
				error[0] = e.getMessage();
			}
		}, () -> {
			if (error[0] == null)
				table.setInput(result);
			else
				MsgBox.error("#Search failed", error[0]);
		});
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

	private void bindDownload() {
		Action action = Actions.create("#Download", Icon.DOWNLOAD.des(), () -> {
			List<Descriptor> selected = Viewers.getAllSelected(table);
			if (selected.isEmpty())
				return;
			List<Ref> refs = selected.stream()
					.map(d -> d.toRef())
					.collect(Collectors.toList());
			DownloadDialog.open(con, refs);
		});
		Actions.bind(table, action);
	}

}
