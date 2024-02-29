package app.navi.actions;

import app.App;
import app.M;
import app.editors.Editors;
import app.navi.RefElement;
import app.rcp.Icon;
import app.store.Data;
import app.util.MsgBox;
import app.util.UI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DuplicateAction extends Action {

	private final RefElement e;

	public DuplicateAction(RefElement e) {
		this.e = e;
		setText(M.Duplicate);
		setImageDescriptor(Icon.SAVE_AS.des());
	}

	@Override
	public void run() {
		if (e == null || e.ref == null || !e.ref.isValid())
			return;
		InputDialog d = new InputDialog(UI.shell(), M.SaveAs,
			M.SaveAs_Message + ":", App.s(e.ref.getName()), null);
		if (d.open() != Window.OK)
			return;
		String name = d.getValue();
		try {
			IDataSet ds = duplicate(name);
			if (ds == null) {
				MsgBox.error("#Could not duplicate data set type="
					+ e.ref.getType() + " id=" + e.ref.getUUID());
				return;
			}
			Data.save(ds);
			Editors.open(Ref.of(ds));
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to duplicate data set " + e.ref, ex);
			MsgBox.error("#Could not duplicate data set type="
				+ e.ref.getType() + " id=" + e.ref.getUUID());
		}
	}

	private IDataSet duplicate(String name) {
		return switch (e.ref.getType()) {
			case CONTACT -> contact(name, e.ref);
			case FLOW -> flow(name, e.ref);
			case FLOW_PROPERTY -> flowProperty(name, e.ref);
			case IMPACT_METHOD -> impactMethod(name, e.ref);
			case PROCESS -> process(name, e.ref);
			case SOURCE -> source(name, e.ref);
			case UNIT_GROUP -> unitGroup(name, e.ref);
			default -> null;
		};
	}

	private Contact contact(String name, Ref ref) {
		var contact = App.store().get(Contact.class, ref.getUUID());
		contact.withContactInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withName()
			.add(LangString.of(name, App.lang()));
		return contact;
	}

	private Flow flow(String name, Ref ref) {
		var flow = App.store().get(Flow.class, ref.getUUID());
		flow.withFlowInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withFlowName()
			.withBaseName()
			.add(LangString.of(name, App.lang()));
		return flow;
	}

	private FlowProperty flowProperty(String name, Ref ref) {
		var property = App.store().get(FlowProperty.class, ref.getUUID());
		property.withFlowPropertyInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withName()
			.add(LangString.of(name, App.lang()));
		return property;
	}

	private ImpactMethod impactMethod(String name, Ref ref) {
		var method = App.store().get(ImpactMethod.class, ref.getUUID());
		method.withMethodInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withName()
			.add(LangString.of(name, App.lang()));
		return method;
	}

	private Process process(String name, Ref ref) {
		var process = App.store().get(Process.class, ref.getUUID());
		process.withProcessInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withProcessName()
			.withBaseName()
			.add(LangString.of(name, App.lang()));
		return process;
	}

	private Source source(String name, Ref ref) {
		var source = App.store().get(Source.class, ref.getUUID());
		source.withSourceInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withName()
			.add(LangString.of(name, App.lang()));
		return source;
	}

	private UnitGroup unitGroup(String name, Ref ref) {
		var group = App.store().get(UnitGroup.class, ref.getUUID());
		group.withUnitGroupInfo()
			.withDataSetInfo()
			.withUUID(UUID.randomUUID().toString())
			.withName()
			.add(LangString.of(name, App.lang()));
		return group;
	}

}
