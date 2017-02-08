package app.navi.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ProcessType;
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

import app.App;
import app.editors.Editors;
import app.navi.CategoryElement;
import app.navi.NavigationElement;
import app.navi.Navigator;
import app.navi.RefElement;
import app.navi.TypeElement;
import app.rcp.Icon;
import app.util.MsgBox;
import epd.model.Xml;

public class NewDataSetAction extends Action {

	private final NavigationElement parent;
	private DataSetType type;

	public NewDataSetAction(NavigationElement elem) {
		if (elem instanceof RefElement)
			parent = elem.getParent();
		else
			parent = elem;
		type = getType(elem);
		setImageDescriptor(Icon.des(type));
		setText(getLabel());
		setToolTipText(getLabel());
	}

	private DataSetType getType(NavigationElement elem) {
		if (elem == null)
			return null;
		if (elem instanceof TypeElement)
			return ((TypeElement) elem).type;
		if (elem instanceof RefElement) {
			Ref ref = ((RefElement) elem).ref;
			if (ref != null && ref.type != null)
				return ref.type;
		}
		return getType(elem.getParent());
	}

	private String getLabel() {
		if (type == null)
			return "#No type";
		switch (type) {
		case CONTACT:
			return "#New contact";
		case FLOW:
			return "#New flow";
		case FLOW_PROPERTY:
			return "#New flow property";
		case LCIA_METHOD:
			return "#New LCIA method";
		case PROCESS:
			return "#New EPD";
		case SOURCE:
			return "#New source";
		case UNIT_GROUP:
			return "#New unit group";
		default:
			return "#No type";
		}
	}

	@Override
	public void run() {
		IDataSet ds = make();
		if (ds == null)
			return;
		try {
			App.store.put(ds);
			App.index.add(ds);
			App.dumpIndex();
			Navigator.refresh(parent);
			Editors.open(Ref.of(ds));
		} catch (Exception e) {
			MsgBox.error("#Failed to create data set", e.getMessage());
		}
	}

	private IDataSet make() {
		if (type == null)
			return null;
		switch (type) {
		case CONTACT:
			return makeContact();
		case FLOW:
			return makeFlow();
		case FLOW_PROPERTY:
			return makeFlowProperty();
		case LCIA_METHOD:
			return makeMethod();
		case PROCESS:
			return makeEpd();
		case SOURCE:
			return makeSource();
		case UNIT_GROUP:
			return makeUnitGroup();
		default:
			return null;
		}
	}

	private Contact makeContact() {
		Contact c = new Contact();
		with(Contacts.dataSetInfo(c), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, "New contact", App.lang);
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		Contacts.dataEntry(c).timeStamp = Xml.now();
		Contacts.publication(c).version = "00.00.000";
		return c;
	}

	private Flow makeFlow() {
		Flow f = new Flow();
		Flows.dataSetInfo(f).uuid = UUID.randomUUID().toString();
		Classification category = getClassification();
		if (category != null)
			Flows.classifications(f).add(category);
		LangString.set(Flows.flowName(f).baseName, "New flow", App.lang);
		Flows.dataEntry(f).timeStamp = Xml.now();
		Flows.publication(f).version = "00.00.000";
		return f;
	}

	private FlowProperty makeFlowProperty() {
		FlowProperty fp = new FlowProperty();
		with(FlowProperties.dataSetInfo(fp), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, "New flow property", App.lang);
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		FlowProperties.dataEntry(fp).timeStamp = Xml.now();
		FlowProperties.publication(fp).version = "00.00.000";
		return fp;
	}

	private LCIAMethod makeMethod() {
		LCIAMethod m = new LCIAMethod();
		with(Methods.dataSetInfo(m), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, "New LCIA method", App.lang);
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		Methods.dataEntry(m).timeStamp = Xml.now();
		Methods.publication(m).version = "00.00.000";
		return m;
	}

	private Process makeEpd() {
		Process p = new Process();
		with(Processes.dataSetInfo(p), info -> {
			info.uuid = UUID.randomUUID().toString();
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		Processes.method(p).processType = ProcessType.EPD;
		LangString.set(Processes.processName(p).name, "New EPD", App.lang);
		Processes.dataEntry(p).timeStamp = Xml.now();
		Processes.publication(p).version = "00.00.000";
		return p;
	}

	private Source makeSource() {
		Source s = new Source();
		with(Sources.dataSetInfo(s), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, "New source", App.lang);
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		Sources.dataEntry(s).timeStamp = Xml.now();
		Sources.publication(s).version = "00.00.000";
		return s;
	}

	private UnitGroup makeUnitGroup() {
		UnitGroup g = new UnitGroup();
		with(UnitGroups.dataSetInfo(g), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, "New unit group", App.lang);
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		UnitGroups.dataEntry(g).timeStamp = Xml.now();
		UnitGroups.publication(g).version = "00.00.000";
		return g;
	}

	private <T> void with(T val, Consumer<T> fn) {
		fn.accept(val);
	}

	private Classification getClassification() {
		List<Category> cats = new ArrayList<>();
		categories(parent, cats);
		if (cats.isEmpty())
			return null;
		Collections.sort(cats, (c1, c2) -> c1.level - c2.level);
		Classification c = new Classification();
		c.categories.addAll(cats);
		return c;
	}

	private void categories(NavigationElement elem, List<Category> list) {
		if (!(elem instanceof CategoryElement))
			return;
		CategoryElement e = (CategoryElement) elem;
		if (e.getCategory() == null)
			return;
		list.add(e.getCategory());
		categories(e.getParent(), list);
	}

}
