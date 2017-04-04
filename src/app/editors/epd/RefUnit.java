package app.editors.epd;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
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

import app.App;

class RefUnit {

	private RefUnit() {
	}

	static String get(Process p) {
		try {
			Exchange e = refExchange(p);
			if (e == null || e.flow == null || !e.flow.isValid())
				return "";
			Unit unit = getUnit(e.flow);
			return unit == null ? "" : unit.name;
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(RefUnit.class);
			log.error("failed to get reference unit for " + p, p);
			return "";
		}
	}

	private static Exchange refExchange(Process p) {
		QuantitativeReference qRef = Processes.getQuantitativeReference(p);
		if (qRef == null)
			return null;
		for (Exchange e : p.exchanges) {
			if (qRef.referenceFlows.contains(e.id))
				return e;
		}
		return null;
	}

	private static Unit getUnit(Ref flowRef) throws Exception {
		Flow flow = App.store.get(Flow.class, flowRef.uuid);
		Ref propertyRef = propertyRef(flow);
		if (propertyRef == null)
			return null;
		FlowProperty prop = App.store.get(FlowProperty.class, propertyRef.uuid);
		if (prop == null)
			return null;
		Ref unitGroupRef = FlowProperties.getUnitGroupRef(prop);
		if (unitGroupRef == null)
			return null;
		UnitGroup group = App.store.get(UnitGroup.class, unitGroupRef.uuid);
		return UnitGroups.getReferenceUnit(group);
	}

	private static Ref propertyRef(Flow flow) {
		if (flow == null)
			return null;
		org.openlca.ilcd.flows.QuantitativeReference qRef = Flows
				.getQuantitativeReference(flow);
		if (qRef == null || qRef.referenceFlowProperty == null)
			return null;
		for (FlowPropertyRef propRef : flow.flowProperties) {
			if (propRef.dataSetInternalID == qRef.referenceFlowProperty)
				return propRef.flowProperty;
		}
		return null;
	}

}
