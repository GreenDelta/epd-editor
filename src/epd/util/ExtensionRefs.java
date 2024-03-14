package epd.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.EpdIndicatorResult;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Flows;

import epd.io.conversion.Extensions;
import epd.io.conversion.FlowExtensions;

public final class ExtensionRefs {

	private ExtensionRefs() {
	}

	/**
	 * Collects data set references from extensions elements that
	 * are not directly mapped to the ILCD model.
	 */
	public static Set<Ref> of(IDataSet ds) {
		if (ds instanceof Process)
			return of((Process) ds);
		if (ds instanceof Flow)
			return of((Flow) ds);
		return Collections.emptySet();
	}

	private static Set<Ref> of(Process p) {
		var epd = Extensions.read(p);
		var refs = new HashSet<Ref>();

		for (var result : EpdIndicatorResult.allOf(p)) {
			if (result.indicator() == null)
				continue;
			var iRef = result.indicator();
			if (iRef.isValid()) {
				refs.add(iRef);
			}
			var uRef = result.unitGroup();
			if (uRef.isValid()) {
				refs.add(iRef);
			}
		}

		epd.publishers.stream()
			.filter(Ref::isValid)
			.forEach(refs::add);
		Epds.getOriginalEpds(epd.process)
				.stream()
			.filter(Ref::isValid)
			.forEach(refs::add);
		return refs;
	}

	private static Set<Ref> of(Flow f) {
		var type = Flows.getType(f);
		if (type != FlowType.PRODUCT_FLOW)
			return Collections.emptySet();
		var product = FlowExtensions.read(f);
		return Stream.of(
				product.genericFlow,
				product.vendor,
				product.documentation)
			.filter(r -> r != null && r.isValid())
			.collect(Collectors.toSet());
	}
}
