package app.store.validation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

	private final HashMap<String, List<RefStatus>> messages = new HashMap<>();

	private final List<File> files = new ArrayList<>();

	public Validation() {
		files.add(App.store.getRootFolder());
	}

	public Validation(Collection<Ref> refs) {
		for (Ref ref : refs) {
			File f = App.store.getFile(ref);
			if (f == null || !f.exists()) {
				RefStatus message = RefStatus.error(ref,
						"#Invalid reference");
				messages.put(ref.uuid,
						Collections.singletonList(message));
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
			add(Event.toStatus(e));
		}
	}

	private void add(RefStatus m) {
		if (m.ref == null || m.ref.uuid == null)
			return;
		List<RefStatus> list = messages.get(m.ref.uuid);
		if (list == null) {
			list = new ArrayList<>();
			messages.put(m.ref.uuid, list);
		}
		list.add(m);
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
		List<RefStatus> all = new ArrayList<>();
		for (List<RefStatus> list : messages.values()) {
			boolean filterInfos = hasIssues(list);
			for (RefStatus s : list) {
				boolean isInfo = s.value == RefStatus.OK ||
						s.value == RefStatus.INFO;
				if (isInfo && filterInfos)
					continue;
				all.add(s);
			}
		}
		return all;
	}

	private boolean hasIssues(List<RefStatus> list) {
		for (RefStatus s : list) {
			if (s.value == RefStatus.ERROR
					|| s.value == RefStatus.WARNING) {
				return true;
			}
		}
		return false;
	}

}
