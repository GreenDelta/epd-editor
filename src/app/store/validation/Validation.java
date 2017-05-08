package app.store.validation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.Ref;

import com.okworx.ilcd.validation.SchemaValidator;
import com.okworx.ilcd.validation.ValidatorChain;
import com.okworx.ilcd.validation.XSLTStylesheetValidator;
import com.okworx.ilcd.validation.events.EventsList;
import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.profile.Profile;
import com.okworx.ilcd.validation.reference.IDatasetReference;
import com.okworx.ilcd.validation.reference.ReferenceBuilder;

import app.App;
import epd.model.RefStatus;

public class Validation implements IRunnableWithProgress {

	private final HashMap<String, RefStatus> status = new HashMap<>();

	private final List<File> files = new ArrayList<>();

	public Validation() {
		files.add(App.store.getRootFolder());
	}

	public Validation(Collection<Ref> refs) {
		for (Ref ref : refs) {
			File f = App.store.getFile(ref);
			if (f == null || !f.exists()) {
				status.put(ref.uuid, RefStatus.error(ref,
						"#Invalid reference"));
				continue;
			}
			files.add(f);
		}
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		ValidatorChain validator = getValidator();
		validator.validate();
		EventsList events = validator.getEventsList();
		for (IValidationEvent e : events.getEvents()) {
			if (e == null || e.getReference() == null)
				continue;
			IDatasetReference iRef = e.getReference();
			RefStatus s = status.get(iRef.getUuid());
			if (s == null || s.value < Event.statusValue(e.getSeverity()))
				status.put(iRef.getUuid(), Event.toStatus(e));
		}
	}

	private ValidatorChain getValidator() throws InvocationTargetException {
		try {
			Profile profile = ValidationProfiles.getActive();
			ValidatorChain chain = new ValidatorChain();
			chain.add(new SchemaValidator());
			chain.add(new XSLTStylesheetValidator());
			chain.setProfile(profile);
			chain.setReportSuccesses(true);
			HashMap<String, IDatasetReference> valRefs = new HashMap<>();
			for (File f : files) {
				ReferenceBuilder refBuild = new ReferenceBuilder();
				refBuild.build(f);
				valRefs.putAll(refBuild.getReferences());
			}
			chain.setObjectsToValidate(valRefs);
			// chain.setUpdateEventListener(this); TODO: show validation
			// progress
			return chain;
		} catch (Exception e) {
			throw new InvocationTargetException(e,
					"Could not create validator");
		}
	}

	public List<RefStatus> getStatus() {
		return new ArrayList<>(status.values());
	}

}
