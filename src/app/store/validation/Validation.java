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
import com.okworx.ilcd.validation.reference.IDatasetReference;
import com.okworx.ilcd.validation.reference.ReferenceBuilder;

import app.App;
import epd.model.RefStatus;

public class Validation implements IRunnableWithProgress {

	private final HashMap<String, List<RefStatus>> messages = new HashMap<>();

	private final List<File> files = new ArrayList<>();

	public Validation() {
		files.add(App.store().getRootFolder());
	}

	public Validation(Collection<Ref> refs) {
		for (var ref : refs) {
			var f = App.store().getFile(ref);
			if (f == null || !f.exists()) {
				var message = RefStatus.error(ref, "Invalid reference");
				messages.put(ref.getUUID(),
						Collections.singletonList(message));
				continue;
			}
			files.add(f);
		}
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		var validator = getValidator();
		validator.validate();
		var events = validator.getEventsList();
		for (var e : events.getEvents()) {
			if (e == null || e.getReference() == null)
				continue;
			add(Event.toStatus(e));
		}
	}

	private void add(RefStatus s) {
		if (s.ref() == null || s.ref().getUUID() == null)
			return;
		var list = messages.computeIfAbsent(
				s.ref().getUUID(), k -> new ArrayList<>());
		if (list.isEmpty()) {
			list.add(s);
			return;
		}
		if (s.isInfo() || s.isOk())
			return;
		list.removeIf(si -> si.isOk() || si.isInfo());
		list.add(s);
	}

	private ValidatorChain getValidator() throws InvocationTargetException {
		try {
			var profile = ValidationProfiles.getActive();
			var chain = new ValidatorChain();
			chain.add(new SchemaValidator());
			chain.add(new XSLTStylesheetValidator());
			chain.setProfile(profile);
			chain.setReportSuccesses(true);
			var valRefs = new HashMap<String, IDatasetReference>();
			for (File f : files) {
				var refBuild = new ReferenceBuilder();
				refBuild.build(f);
				valRefs.putAll(refBuild.getReferences());
			}
			chain.setObjectsToValidate(valRefs);
			return chain;
		} catch (Exception e) {
			throw new InvocationTargetException(e,
					"Could not create validator");
		}
	}

	public List<RefStatus> getStatus() {
		var all = new ArrayList<RefStatus>();
		for (var list : messages.values()) {
			boolean filterInfos = hasIssues(list);
			for (var s : list) {
				boolean isInfo = s.isOk() || s.isInfo();
				if (isInfo && filterInfos)
					continue;
				all.add(s);
			}
		}
		return all;
	}

	private boolean hasIssues(List<RefStatus> list) {
		for (RefStatus s : list) {
			if (s.isError() || s.isWarning()) {
				return true;
			}
		}
		return false;
	}

}
