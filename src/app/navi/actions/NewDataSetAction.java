package app.navi.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.FlowType;
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
import app.M;
import app.editors.Editors;
import app.navi.CategoryElement;
import app.navi.NavigationElement;
import app.navi.RefElement;
import app.navi.TypeElement;
import app.rcp.Icon;
import app.store.Data;
import app.util.MsgBox;
import epd.model.Xml;

public class NewDataSetAction extends Action {

	private final NavigationElement parent;
	private final DataSetType type;

	public NewDataSetAction(NavigationElement elem) {
		parent = elem instanceof RefElement
				? elem.getParent()
				: elem;
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
			return M.None;
		return switch (type) {
		case CONTACT -> M.NewContact;
		case FLOW -> M.NewProduct;
		case FLOW_PROPERTY -> M.NewFlowProperty;
		case LCIA_METHOD -> M.NewLCIAMethod;
		case PROCESS -> M.NewEPD;
		case SOURCE -> M.NewSource;
		case UNIT_GROUP -> M.NewUnitGroup;
		default -> M.None;
		};
	}

	@Override
	public void run() {
		IDataSet ds = make();
		if (ds == null)
			return;
		try {
			Data.save(ds);
			Editors.open(Ref.of(ds));
		} catch (Exception e) {
			MsgBox.error("#Failed to create data set", e.getMessage());
		}
	}

	private IDataSet make() {
		if (type == null)
			return null;
		return switch (type) {
		case CONTACT -> makeContact();
		case FLOW -> makeFlow();
		case FLOW_PROPERTY -> makeFlowProperty();
		case LCIA_METHOD -> makeMethod();
		case PROCESS -> makeEpd();
		case SOURCE -> makeSource();
		case UNIT_GROUP -> makeUnitGroup();
		default -> null;
		};
	}

	private Contact makeContact() {
		Contact c = new Contact();
		c.version = "1.1";
		with(Contacts.dataSetInfo(c), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, M.NewContact, App.lang());
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
		f.version = "1.1";
		Flows.dataSetInfo(f).uuid = UUID.randomUUID().toString();
		Classification category = getClassification();
		if (category != null)
			Flows.classifications(f).add(category);
		LangString.set(Flows.flowName(f).baseName, M.NewProduct, App.lang());
		Flows.inventoryMethod(f).flowType = FlowType.PRODUCT_FLOW;
		Flows.dataEntry(f).timeStamp = Xml.now();
		Flows.publication(f).version = "00.00.000";
		return f;
	}

	private FlowProperty makeFlowProperty() {
		FlowProperty fp = new FlowProperty();
		fp.version = "1.1";
		with(FlowProperties.dataSetInfo(fp), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, M.NewFlowProperty, App.lang());
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
		m.version = "1.1";
		with(Methods.forceDataSetInfo(m), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, M.NewLCIAMethod, App.lang());
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		Methods.forceDataEntry(m).timeStamp = Xml.now();
		Methods.forcePublication(m).version = "00.00.000";
		return m;
	}

	private Process makeEpd() {
		var p = new Process();
		p.version = "1.1";
		with(Processes.forceDataSetInfo(p), info -> {
			info.uuid = UUID.randomUUID().toString();
			Classification category = getClassification();
			if (category != null)
				info.classifications.add(category);
		});
		Processes.forceMethod(p).processType = ProcessType.EPD;
		LangString.set(Processes.forceProcessName(p).name, M.NewEPD, App.lang());
		Processes.forceDataEntry(p).timeStamp = Xml.now();
		Processes.forcePublication(p).version = "00.00.000";
		return p;
	}

	private Source makeSource() {
		Source s = new Source();
		s.version = "1.1";
		with(Sources.dataSetInfo(s), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, M.NewSource, App.lang());
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
		g.version = "1.1";
		with(UnitGroups.dataSetInfo(g), info -> {
			info.uuid = UUID.randomUUID().toString();
			LangString.set(info.name, M.NewUnitGroup, App.lang());
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
		var cats = new ArrayList<Category>();
		categories(parent, cats);
		if (cats.isEmpty())
			return null;
		cats.sort(Comparator.comparingInt(c -> c.level));
		Classification c = new Classification();
		c.categories.addAll(cats);
		return c;
	}

	private void categories(NavigationElement elem, List<Category> list) {
		if (!(elem instanceof CategoryElement))
			return;
		var e = (CategoryElement) elem;
		if (e.getCategory() == null)
			return;
		list.add(e.getCategory());
		categories(e.getParent(), list);
	}

}
