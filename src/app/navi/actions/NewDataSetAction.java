package app.navi.actions;

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
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.DataSets;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Processes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
			return ((TypeElement) elem).getContent();
		if (elem instanceof RefElement) {
			Ref ref = ((RefElement) elem).getContent();
			if (ref != null && ref.getType() != null)
				return ref.getType();
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
			case IMPACT_METHOD -> M.NewLCIAMethod;
			case PROCESS -> M.NewEPD;
			case SOURCE -> M.NewSource;
			case UNIT_GROUP -> M.NewUnitGroup;
			default -> M.None;
		};
	}

	@Override
	public void run() {
		var ds = make();
		if (ds == null)
			return;
		try {
			Data.save(ds);
			Editors.open(Ref.of(ds));
		} catch (Exception e) {
			MsgBox.error("Failed to create data set", e.getMessage());
		}
	}

	private IDataSet make() {
		if (type == null)
			return null;
		return switch (type) {
			case CONTACT -> init(new Contact());
			case FLOW -> {
				var f = init(new Flow());
				Flows.withInventoryMethod(f)
					.withFlowType(FlowType.PRODUCT_FLOW);
				yield f;
			}
			case FLOW_PROPERTY -> init(new FlowProperty());
			case IMPACT_METHOD -> init(new ImpactMethod());
			case PROCESS -> {
				var p = init(new Process());
				p.withEpdVersion("1.2");
				Processes.withInventoryMethod(p)
					.withProcessType(ProcessType.EPD);
				yield p;
			}
			case SOURCE -> init(new Source());
			case UNIT_GROUP -> init(new UnitGroup());
			default -> null;
		};
	}

	private <T extends IDataSet> T init(T ds) {
		DataSets.withUUID(ds, UUID.randomUUID().toString());
		DataSets.withBaseName(ds, LangString.of(getLabel(), App.lang()));
		var category = getClassification();
		if (category != null) {
			DataSets.withClassifications(ds).add(category);
		}
		DataSets.withTimeStamp(ds, Xml.now());
		DataSets.withVersion(ds, "00.00.000");
		return ds;
	}

	private Classification getClassification() {
		var cats = new ArrayList<Category>();
		categories(parent, cats);
		if (cats.isEmpty())
			return null;
		cats.sort(Comparator.comparingInt(Category::getLevel));
		var c = new Classification();
		c.withCategories().addAll(cats);
		return c;
	}

	private void categories(NavigationElement elem, List<Category> list) {
		if (!(elem instanceof CategoryElement e))
			return;
		if (e.getCategory() == null)
			return;
		list.add(e.getCategory());
		categories(e.getParent(), list);
	}

}
