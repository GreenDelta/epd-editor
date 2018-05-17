package epd.util;

import java.util.Arrays;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Flows;

import app.App;
import app.store.EpdProfiles;
import epd.io.conversion.FlowExtensions;
import epd.io.conversion.Extensions;
import epd.model.EpdDataSet;
import epd.model.EpdProduct;
import epd.model.IndicatorMapping;
import epd.model.IndicatorResult;

public final class ExtensionRefs {

	private ExtensionRefs() {
	}

	/**
	 * Collects data set references from extensions and adds them to the given
	 * list if they are not contained yet.
	 */
	public static void collect(IDataSet ds, List<Ref> refs) {
		if (ds == null || refs == null)
			return;
		if (ds instanceof Process)
			add((Process) ds, refs);
		if (ds instanceof Flow)
			add((Flow) ds, refs);
	}

	private static void add(Process p, List<Ref> refs) {
		List<IndicatorMapping> indicators = EpdProfiles.get();
		EpdDataSet epd = Extensions.read(p, indicators);
		for (IndicatorResult r : epd.results) {
			IndicatorMapping im = EpdProfiles.get(r.indicator);
			if (im == null)
				continue;
			indicatorRef(im, refs);
			unitGroupRef(im, refs);
		}
	}

	private static void indicatorRef(IndicatorMapping im, List<Ref> refs) {
		if (im == null || im.indicator == null)
			return;
		Ref ref = new Ref();
		ref.uuid = im.indicatorRefId;
		if (im.indicator.isInventoryIndicator())
			ref.type = DataSetType.FLOW;
		else
			ref.type = DataSetType.LCIA_METHOD;
		LangString.set(ref.name, im.indicatorLabel, App.lang());
		if (!ref.isValid() || refs.contains(ref))
			return;
		refs.add(ref);
	}

	private static void unitGroupRef(IndicatorMapping im, List<Ref> refs) {
		Ref ref = new Ref();
		ref.uuid = im.unitRefId;
		ref.type = DataSetType.UNIT_GROUP;
		LangString.set(ref.name, im.unitLabel, App.lang());
		if (!ref.isValid() || refs.contains(ref))
			return;
		refs.add(ref);
	}

	private static void add(Flow f, List<Ref> refs) {
		FlowType type = Flows.getType(f);
		if (type != FlowType.PRODUCT_FLOW)
			return;
		EpdProduct p = FlowExtensions.read(f);
		List<Ref> list = Arrays.asList(p.genericFlow, p.vendor,
				p.documentation);
		for (Ref ref : list) {
			if (ref == null || refs.contains(ref))
				continue;
			refs.add(ref);
		}
	}
}