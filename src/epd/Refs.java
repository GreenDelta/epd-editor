package epd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Processes;

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

		refs.addAll(getAllProcessInfo(epd));

		var inventoryMethod = Processes.getInventoryMethod(epd);
		if (inventoryMethod != null) {
			refs.addAll(inventoryMethod.getSources());
		}

		var representativeness = Processes.getRepresentativeness(epd);
		if (representativeness != null) {
			refs.addAll(representativeness.getDataHandlingSources());
			refs.addAll(representativeness.getSources());
		}

		refs.addAll(Epds.withOriginalEpds(epd));

		var modelling = Processes.getModelling(epd);
		if (modelling != null) {
			refs.addAll(getAll(modelling));
		}

		refs.addAll(getAllAdmin(epd));

		return refs;
	}


	public static List<Ref> getAllEditable(Flow product) {
		var genericFlow = getGenericFlow(product);
		return genericFlow != null
			? Collections.singletonList(genericFlow)
			: Collections.emptyList();
	}

	private static List<Ref> getAll(Modelling modelling) {
		var refs = new ArrayList<Ref>();

		modelling.getComplianceDeclarations().stream()
			.map(ComplianceDeclaration::getSystem)
			.filter(Objects::nonNull)
			.forEach(refs::add);

		var validation = modelling.getValidation();
		if (validation != null) {
			validation.getReviews().stream()
				.map(Review::getReviewers)
				.forEach(refs::addAll);
			validation.getReviews().stream()
				.map(Review::getReport)
				.filter(Objects::nonNull)
				.forEach(refs::add);
		}

		return refs;
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

	private static List<Ref> getAllProcessInfo(Process epd) {
		var refs = new ArrayList<Ref>();

		var dataSetInfo = Epds.getDataSetInfo(epd);
		if (dataSetInfo != null) {
			refs.addAll(dataSetInfo.getExternalDocs());
		}

		var technology = Epds.getTechnology(epd);
		if (technology != null) {
			refs.addAll(getAll(technology));
		}

		return refs;
	}

	private static List<Ref> getAll(Technology technology) {
		var refs = new ArrayList<Ref>();

		var pictogram = technology.getPictogram();
		if (pictogram != null) {
			refs.add(technology.getPictogram());
		}

		refs.addAll(technology.getPictures());

		return refs;
	}

	private static List<Ref> getAllAdmin(Process epd) {
		var refs = new ArrayList<Ref>();

		var commissionersAndGoals = Processes.getCommissionerAndGoal(epd);
		if (commissionersAndGoals != null) {
			refs.addAll(commissionersAndGoals.getCommissioners());
		}

		var dataEntry = Processes.getDataEntry(epd);
		if (dataEntry != null) {
			refs.addAll(dataEntry.getFormats());
			var documentor = dataEntry.getDocumentor();
			if (documentor != null) {
				refs.add(documentor);
			}
		}

		var dataGenerator = Processes.getDataGenerator(epd);
		if (dataGenerator != null) {
			refs.addAll(dataGenerator.getContacts());
		}

		var publication = Processes.getPublication(epd);
		if (publication != null) {
			refs.addAll(getAll(publication));
		}

		refs.addAll(Epds.withPublishers(epd));

		return refs;
	}

	private static List<Ref> getAll(Publication publication) {
		var refs = new ArrayList<Ref>();

		var registrationAuthority = publication.getRegistrationAuthority();
		if (registrationAuthority != null) {
			refs.add(publication.getRegistrationAuthority());
		}

		var owner = publication.getOwner();
		if (owner != null) {
			refs.add(publication.getOwner());
		}

		var republication = publication.getRepublication();
		if (republication != null) {
			refs.add(publication.getRepublication());
		}

		refs.addAll(publication.getPrecedingVersions());

		return refs;
	}


	private static List<Ref> getAllProductInfo(Flow product) {
		var refs = new ArrayList<Ref>();


		return refs;
	}

}
