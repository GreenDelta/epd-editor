package epd.refs;

import static epd.refs.Refs.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.epd.EpdInfoExtension;
import org.openlca.ilcd.flows.epd.EpdMethodExtension;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Contacts;
import org.openlca.ilcd.util.FlowProperties;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Sources;

class DataSetRefs {

	static List<Ref> allEditableOf(IDataSet ds) {
		if (ds instanceof Process epd)
			return allEditableOf(epd);
		else if (ds instanceof Flow product)
			return allEditableOf(product);
		else if (ds instanceof Contact contact)
			return allEditableOf(contact);
		else if (ds instanceof Source source)
			return allEditableOf(source);
		else if (ds instanceof FlowProperty prop)
			return allEditableOf(prop);
		else return Collections.emptyList();
	}

	private static List<Ref> allEditableOf(Process epd) {
		if (epd == null)
			return Collections.emptyList();
		return EpdRefs.of(epd)
			.declaredProduct()
			.externalDocumentation()
			.flowDiagramsAndPictures()
			.methodDetails()
			.dataQualitySources()
			.dataSources()
			.complianceSystems()
			.originalEpds()
			.reviewersAndReviewReports()
			.commissioners()
			.dataDocumentor()
			.dataGenerators()
			.dataFormats()
			.registrationAuthorities()
			.owner()
			.publishers()
			.precedingDataSets()
			.asList();
	}

	private static List<Ref> allEditableOf(Flow product) {
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

	private static List<Ref> allEditableOf(Contact contact) {
		var info = Contacts.getDataSetInfo(contact);
		return info != null && info.getLogo() != null
			? Collections.singletonList(info.getLogo())
			: Collections.emptyList();
	}

	private static List<Ref> allEditableOf(Source source) {
		var refs = new ArrayList<Ref>();
		// logo
		add(refs, Sources.getDataSetInfo(source),
			org.openlca.ilcd.sources.DataSetInfo::getLogo);
		// contacts
		addAll(refs, Sources.getDataSetInfo(source),
			org.openlca.ilcd.sources.DataSetInfo::getContacts);
		return refs;
	}

	private static List<Ref> allEditableOf(FlowProperty prop) {
		var group = FlowProperties.getUnitGroupRef(prop);
		return group != null
			? Collections.singletonList(group)
			: Collections.emptyList();
	}

}
