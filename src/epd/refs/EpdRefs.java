package epd.refs;

import static epd.refs.Refs.*;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.InventoryMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Epds;

class EpdRefs {

	private final Process epd;
	private final List<Ref> refs;

	private EpdRefs(Process epd) {
		this.epd = epd;
		this.refs = new ArrayList<>();
	}

	static EpdRefs of(Process epd) {
		return new EpdRefs(epd);
	}

	// region general information

	EpdRefs declaredProduct() {
		var refFlows = Epds.getReferenceFlows(epd);
		for (var e : epd.getExchanges()) {
			if (refFlows.contains(e.getId())) {
				add(refs, e, Exchange::getFlow);
			}
		}
		return this;
	}

	EpdRefs externalDocumentation() {
		addAll(refs, Epds.getDataSetInfo(epd), DataSetInfo::getExternalDocs);
		return this;
	}

	EpdRefs flowDiagramsAndPictures() {
		add(refs, Epds.getTechnology(epd), Technology::getPictogram);
		addAll(refs, Epds.getTechnology(epd), Technology::getPictures);
		return this;
	}

	// endregion

	// region modelling & validation

	EpdRefs methodDetails() {
		addAll(refs, Epds.getInventoryMethod(epd), InventoryMethod::getSources);
		return this;
	}

	EpdRefs dataQualitySources() {
		addAll(refs, Epds.getRepresentativeness(epd),
			Representativeness::getDataHandlingSources);
		return this;
	}

	EpdRefs dataSources() {
		addAll(refs, Epds.getRepresentativeness(epd),
			Representativeness::getSources);
		return this;
	}

	EpdRefs complianceSystems() {
		for (var dec : Epds.getComplianceDeclarations(epd)) {
			add(refs, dec, ComplianceDeclaration::getSystem);
		}
		return this;
	}

	EpdRefs originalEpds() {
		refs.addAll(Epds.getOriginalEpds(epd));
		return this;
	}

	EpdRefs reviewersAndReviewReports() {
		for (var rev : Epds.getReviews(epd)) {
			addAll(refs, rev, Review::getReviewers);
			add(refs, rev, Review::getReport);
		}
		return this;
	}

	// endregion

	// region administrative information

	EpdRefs commissioners() {
		addAll(refs, Epds.getCommissionerAndGoal(epd),
			CommissionerAndGoal::getCommissioners);
		return this;
	}

	EpdRefs dataDocumentor() {
		add(refs, Epds.getDataEntry(epd), DataEntry::getDocumentor);
		return this;
	}

	EpdRefs dataGenerators() {
		addAll(refs, Epds.getDataGenerator(epd), DataGenerator::getContacts);
		return this;
	}

	EpdRefs dataFormats() {
		addAll(refs, Epds.getDataEntry(epd), DataEntry::getFormats);
		return this;
	}

	EpdRefs registrationAuthorities() {
		add(refs, Epds.getPublication(epd),
			Publication::getRegistrationAuthority);
		return this;
	}

	EpdRefs owner() {
		add(refs, Epds.getPublication(epd), Publication::getOwner);
		return this;
	}

	EpdRefs publishers() {
		refs.addAll(Epds.getPublishers(epd));
		return this;
	}

	EpdRefs precedingDataSets() {
		addAll(refs, Epds.getPublication(epd), Publication::getPrecedingVersions);
		return this;
	}

	// end region

	List<Ref> asList() {
		return refs;
	}

}
