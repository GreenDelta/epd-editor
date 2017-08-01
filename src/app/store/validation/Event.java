package app.store.validation;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import com.okworx.ilcd.validation.common.DatasetType;
import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.events.Severity;
import com.okworx.ilcd.validation.reference.IDatasetReference;

import app.App;
import epd.model.RefStatus;

final class Event {

	private Event() {
	}

	static RefStatus toStatus(IValidationEvent e) {
		if (e == null)
			return null;
		IDatasetReference iRef = e.getReference();
		Ref ref = new Ref();
		LangString.set(ref.name, iRef.getName(), App.lang());
		ref.type = type(iRef.getType());
		ref.uri = iRef.getUri();
		ref.uuid = iRef.getUuid();
		ref.version = iRef.getVersion();
		return new RefStatus(statusValue(e.getSeverity()),
				ref, e.getAspect() + ": " + e.getMessage());
	}

	static int statusValue(Severity s) {
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

	private static DataSetType type(DatasetType iType) {
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