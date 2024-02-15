package app.store;

import app.App;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.FlowProperties;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Processes;
import org.openlca.ilcd.util.UnitGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * An utility class for loading references and dependencies. from the local data
 * store.
 */
public class RefDeps {

	private RefDeps() {
	}

	public static String getRefUnit(Ref ref) {
		if (ref == null || !ref.isValid())
			return "";
		return switch (ref.type) {
			case FLOW -> getRefUnit(load(Flow.class, ref));
			case FLOW_PROPERTY -> getRefUnit(load(FlowProperty.class, ref));
			case PROCESS -> getRefUnit(load(Process.class, ref));
			case UNIT_GROUP -> getRefUnit(load(UnitGroup.class, ref));
			default -> "";
		};
	}

	public static <T extends IDataSet> T load(Class<T> type, Ref ref) {
		try {
			return App.store().get(type, ref.uuid);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(RefDeps.class);
			log.error("failed load data set " + ref, e);
			return null;
		}
	}

	public static String getRefUnit(Process p) {
		if (p == null)
			return "";
		Exchange e = getRefExchange(p);
		return getRefUnit(e.flow);
	}

	public static Exchange getRefExchange(Process p) {
		QuantitativeReference qRef = Processes.getQuantitativeReference(p);
		if (qRef == null)
			return null;
		for (Exchange e : p.exchanges) {
			if (qRef.referenceFlows.contains(e.id))
				return e;
		}
		return null;
	}

	public static String getRefUnit(Flow flow) {
		if (flow == null)
			return "";
		Ref propertyRef = propertyRef(flow);
		return getRefUnit(propertyRef);
	}

	private static Ref propertyRef(Flow flow) {
		if (flow == null)
			return null;
		var qRef = Flows.getQuantitativeReference(flow);
		if (qRef == null || qRef.referenceFlowProperty == null)
			return null;
		for (var propRef : Flows.getFlowProperties(flow)) {
			if (Objects.equals(propRef.dataSetInternalID, qRef.referenceFlowProperty))
				return propRef.flowProperty;
		}
		return null;
	}

	public static String getRefUnit(FlowProperty prop) {
		if (prop == null)
			return "";
		Ref unitGroupRef = FlowProperties.getUnitGroupRef(prop);
		return getRefUnit(unitGroupRef);
	}

	public static String getRefUnit(UnitGroup group) {
		Unit unit = UnitGroups.getReferenceUnit(group);
		return unit == null ? "" : unit.name;
	}

}
