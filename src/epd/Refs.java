package epd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.*;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Flows;

public final class Refs {

	private Refs() {
	}

	public static List<Ref> getAllEditable(IDataSet ds) {
		if (ds instanceof Process epd)
			return getAllEditable(epd);
		else if (ds instanceof Flow product) {
			return getAllEditable(product);
		} else return Collections.emptyList();
	}

	public static List<Ref> getAllEditable(Process epd) {
		var refs = new ArrayList<Ref>();

		// general information

		// declared product
		var refFlows = Epds.getReferenceFlows(epd);
		for (var e : epd.getExchanges()) {
			if (refFlows.contains(e.getId())) {
				add(refs, e, Exchange::getFlow);
			}
		}

		// external documentation
		addAll(refs, Epds.getDataSetInfo(epd), DataSetInfo::getExternalDocs);

		// technical flow diagrams & pictures
		add(refs, Epds.getTechnology(epd), Technology::getPictogram);
		addAll(refs, Epds.getTechnology(epd), Technology::getPictures);

		// modelling & validation

		// LCA method details
		addAll(refs, Epds.getInventoryMethod(epd), InventoryMethod::getSources);

		// data quality sources
		addAll(refs, Epds.getRepresentativeness(epd),
			Representativeness::getDataHandlingSources);

		// data sources
		addAll(refs, Epds.getRepresentativeness(epd),
			Representativeness::getSources);

		// compliance declarations
		for (var dec : Epds.getComplianceDeclarations(epd)) {
			add(refs, dec, ComplianceDeclaration::getSystem);
		}

		// original EPDs
		refs.addAll(Epds.getOriginalEpds(epd));

		// reviewers & review reports
		for (var rev : Epds.getReviews(epd)) {
			addAll(refs, rev, Review::getReviewers);
			add(refs, rev, Review::getReport);
		}

		// administrative information

		// commissioners
		addAll(refs, Epds.getCommissionerAndGoal(epd),
			CommissionerAndGoal::getCommissioners);

		// documentor
		add(refs, Epds.getDataEntry(epd), DataEntry::getDocumentor);

		// data generators
		addAll(refs, Epds.getDataGenerator(epd), DataGenerator::getContacts);

		// data formats
		addAll(refs, Epds.getDataEntry(epd), DataEntry::getFormats);

		// publication
		var pub = Epds.getPublication(epd);
		add(refs, pub, Publication::getRegistrationAuthority);
		add(refs, pub, Publication::getOwner);

		// publishers
		refs.addAll(Epds.getPublishers(epd));

		// preceding data sets
		addAll(refs, pub, Publication::getPrecedingVersions);

		return refs;
	}

	private static <T> void addAll(
		List<Ref> refs, T obj, Function<T, List<Ref>> fn
	) {
		if (obj == null)
			return;
		var list = fn.apply(obj);
		if (list == null || list.isEmpty())
			return;
		refs.addAll(list);
	}

	private static <T> void add(List<Ref> refs, T obj, Function<T, Ref> fn) {
		if (obj == null)
			return;
		var ref = fn.apply(obj);
		if (ref != null) {
			refs.add(ref);
		}
	}

	public static List<Ref> getAllEditable(Flow product) {
		var genericFlow = getGenericFlow(product);
		return genericFlow != null
			? Collections.singletonList(genericFlow)
			: Collections.emptyList();
	}

	private static Ref getGenericFlow(Flow product) {
		var info = Flows.getDataSetInfo(product);
		if (info == null)
			return null;

		var extension = info.getEpdExtension();
		if (extension == null)
			return null;

		return extension.getGenericFlow();
	}

}
