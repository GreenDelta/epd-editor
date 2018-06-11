package app.navi.actions;

import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Contacts;
import org.openlca.ilcd.util.FlowProperties;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Methods;
import org.openlca.ilcd.util.Processes;
import org.openlca.ilcd.util.Sources;
import org.openlca.ilcd.util.UnitGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.App;
import app.M;
import app.editors.Editors;
import app.navi.RefElement;
import app.rcp.Icon;
import app.store.Data;
import app.util.MsgBox;
import app.util.UI;

public class DuplicateAction extends Action {

	private final RefElement e;

	public DuplicateAction(RefElement e) {
		this.e = e;
		setText("#Duplicate");
		setImageDescriptor(Icon.SAVE_AS.des());
	}

	@Override
	public void run() {
		if (e == null || e.ref == null || !e.ref.isValid())
			return;
		InputDialog d = new InputDialog(UI.shell(), M.SaveAs,
				"#Save as new data set with the following name:",
				"#Copy of " + App.s(e.ref.name), null);
		if (d.open() != Window.OK)
			return;
		String name = d.getValue();
		try {
			IDataSet ds = duplicate(name);
			if (ds == null) {
				MsgBox.error("#Could not duplicate data set type="
						+ e.ref.type + " id=" + e.ref.uuid);
				return;
			}
			Data.save(ds);
			Editors.open(Ref.of(ds));
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to duplicate data set " + e.ref, ex);
			MsgBox.error("#Could not duplicate data set type="
					+ e.ref.type + " id=" + e.ref.uuid);
		}
	}

	private IDataSet duplicate(String name) throws Exception {
		switch (e.ref.type) {
		case CONTACT:
			return contact(name, e.ref);
		case FLOW:
			return flow(name, e.ref);
		case FLOW_PROPERTY:
			return flowProperty(name, e.ref);
		case LCIA_METHOD:
			return impactMethod(name, e.ref);
		case PROCESS:
			return process(name, e.ref);
		case SOURCE:
			return source(name, e.ref);
		case UNIT_GROUP:
			return unitGroup(name, e.ref);
		default:
			return null;
		}
	}

	private Contact contact(String name, Ref ref) throws Exception {
		var contact = App.store.get(Contact.class, ref.uuid);
		var info = Contacts.dataSetInfo(contact);
		info.uuid = UUID.randomUUID().toString();
		LangString.set(info.name, name, App.lang());
		return contact;
	}

	private Flow flow(String name, Ref ref) throws Exception {
		var flow = App.store.get(Flow.class, ref.uuid);
		var info = Flows.dataSetInfo(flow);
		info.uuid = UUID.randomUUID().toString();
		var flowName = Flows.flowName(flow);
		LangString.set(flowName.baseName, name, App.lang());
		return flow;
	}

	private FlowProperty flowProperty(String name, Ref ref) throws Exception {
		var property = App.store.get(FlowProperty.class, ref.uuid);
		var info = FlowProperties.dataSetInfo(property);
		info.uuid = UUID.randomUUID().toString();
		LangString.set(info.name, name, App.lang());
		return property;
	}

	private LCIAMethod impactMethod(String name, Ref ref) throws Exception {
		var method = App.store.get(LCIAMethod.class, ref.uuid);
		var info = Methods.dataSetInfo(method);
		info.uuid = UUID.randomUUID().toString();
		LangString.set(info.name, name, App.lang());
		return method;
	}

	private Process process(String name, Ref ref) throws Exception {
		var process = App.store.get(Process.class, ref.uuid);
		var info = Processes.dataSetInfo(process);
		info.uuid = UUID.randomUUID().toString();
		var processName = Processes.processName(process);
		LangString.set(processName.name, name, App.lang());
		return process;
	}

	private Source source(String name, Ref ref) throws Exception {
		var source = App.store.get(Source.class, ref.uuid);
		var info = Sources.dataSetInfo(source);
		info.uuid = UUID.randomUUID().toString();
		LangString.set(info.name, name, App.lang());
		return source;
	}

	private UnitGroup unitGroup(String name, Ref ref) throws Exception {
		var group = App.store.get(UnitGroup.class, ref.uuid);
		var info = UnitGroups.dataSetInfo(group);
		info.uuid = UUID.randomUUID().toString();
		LangString.set(info.name, name, App.lang());
		return group;
	}

}
