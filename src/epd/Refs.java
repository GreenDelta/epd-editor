package epd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.epd.EpdInfoExtension;
import org.openlca.ilcd.flows.epd.EpdMethodExtension;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.*;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Contacts;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.FlowProperties;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Sources;

public final class Refs {

	private Refs() {
	}

	public static List<Ref> getAllEditable(IDataSet ds) {
		if (ds instanceof Process epd)
			return getAllEditable(epd);
		else if (ds instanceof Flow product)
			return getAllEditable(product);
		else if (ds instanceof Contact contact)
			return getAllEditable(contact);
		else if (ds instanceof Source source)
			return getAllEditable(source);
		else if (ds instanceof FlowProperty prop)
			return getAllEditable(prop);
		else return Collections.emptyList();
	}

	public static List<Ref> getAllEditable(Process epd) {
		if (epd == null)
			return Collections.emptyList();

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

	public static List<Ref> getAllEditable(Flow product) {
		if (product == null)
			return Collections.emptyList();

		var refs = new ArrayList<Ref>();

		// generic flow
		var info = Flows.getDataSetInfo(product);
		var infoExt = info != null ? info.getEpdExtension() : null;
		add(refs, infoExt, EpdInfoExtension::getGenericFlow);

		// vendor & documentation
		var method = Flows.getInventoryMethod(product);
		var methodExt = method != null ? method.getEpdExtension() : null;
		add(refs, methodExt, EpdMethodExtension::getVendor);
		add(refs, methodExt, EpdMethodExtension::getDocumentation);

		// flow properties
		for (var prop : Flows.getFlowProperties(product)) {
			add(refs, prop, FlowPropertyRef::getFlowProperty);
		}

		return refs;
	}

	public static List<Ref> getAllEditable(Contact contact) {
		var info = Contacts.getDataSetInfo(contact);
		return info != null && info.getLogo() != null
			? Collections.singletonList(info.getLogo())
			: Collections.emptyList();
	}

	public static List<Ref> getAllEditable(Source source) {
		var refs = new ArrayList<Ref>();
		// logo
		add(refs, Sources.getDataSetInfo(source),
			org.openlca.ilcd.sources.DataSetInfo::getLogo);
		// contacts
		addAll(refs, Sources.getDataSetInfo(source),
			org.openlca.ilcd.sources.DataSetInfo::getContacts);
		return refs;
	}

	public static List<Ref> getAllEditable(FlowProperty prop) {
		var group = FlowProperties.getUnitGroupRef(prop);
		return group != null
			? Collections.singletonList(group)
			: Collections.emptyList();
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

}
