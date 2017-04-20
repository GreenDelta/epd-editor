package app.store;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import com.okworx.ilcd.validation.SchemaValidator;
import com.okworx.ilcd.validation.ValidatorChain;
import com.okworx.ilcd.validation.XSLTStylesheetValidator;
import com.okworx.ilcd.validation.common.DatasetType;
import com.okworx.ilcd.validation.events.EventsList;
import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.events.Severity;
import com.okworx.ilcd.validation.reference.IDatasetReference;
import com.okworx.ilcd.validation.util.IUpdateEventListener;

import app.App;
import epd.model.RefStatus;

public class Validation implements IRunnableWithProgress, IUpdateEventListener {

	private final HashMap<String, RefStatus> status = new HashMap<>();

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {

		// TODO: register EPD profile
		ValidatorChain chain = new ValidatorChain();
		SchemaValidator sv = new SchemaValidator();
		chain.add(sv);
		XSLTStylesheetValidator xslt = new XSLTStylesheetValidator();
		chain.add(xslt);
		// CategoryValidator
		// chain.setProfile(profile);

		sv.setObjectsToValidate(App.store.getRootFolder());
		sv.setReportSuccesses(true);
		sv.setUpdateEventListener(this);
		sv.validate();
		EventsList events = sv.getEventsList();
		for (IValidationEvent e : events.getEvents()) {
			if (e == null || e.getReference() == null)
				continue;
			IDatasetReference iRef = e.getReference();
			RefStatus s = status.get(iRef.getUuid());
			if (s == null || s.value < value(e.getSeverity()))
				status.put(iRef.getUuid(), status(e));
		}
	}

	public List<RefStatus> getStatus() {
		return new ArrayList<>(status.values());
	}

	@Override
	public void updateProgress(double percentage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateStatus(String status) {
		// TODO Auto-generated method stub
	}

	private RefStatus status(IValidationEvent e) {
		IDatasetReference iRef = e.getReference();
		Ref ref = new Ref();
		LangString.set(ref.name, iRef.getName(), App.lang());
		ref.type = type(iRef.getType());
		ref.uri = iRef.getUri();
		ref.uuid = iRef.getUuid();
		ref.version = iRef.getVersion();
		return new RefStatus(value(e.getSeverity()), ref, e.getMessage());
	}

	private int value(Severity s) {
		if (s == null)
			return RefStatus.INFO;
		switch (s) {
		case ERROR:
			return RefStatus.ERROR;
		case SUCCESS:
			return RefStatus.OK;
		case WARNING:
			return RefStatus.WARNING;
		default:
			return RefStatus.INFO;
		}
	}

	private DataSetType type(DatasetType iType) {
		if (iType == null)
			return null;
		switch (iType) {
		case CONTACT:
			return DataSetType.CONTACT;
		case EXTERNAL_FILE:
			return DataSetType.EXTERNAL_FILE;
		case FLOW:
			return DataSetType.FLOW;
		case FLOWPROPERTY:
			return DataSetType.FLOW_PROPERTY;
		case LCIAMETHOD:
			return DataSetType.LCIA_METHOD;
		case PROCESS:
			return DataSetType.PROCESS;
		case SOURCE:
			return DataSetType.SOURCE;
		case UNITGROUP:
			return DataSetType.UNIT_GROUP;
		default:
			return null;
		}
	}

}
