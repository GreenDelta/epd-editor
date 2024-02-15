package app.store.validation;

import app.App;
import com.okworx.ilcd.validation.common.DatasetType;
import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.events.Severity;
import com.okworx.ilcd.validation.reference.IDatasetReference;
import epd.model.RefStatus;
import org.apache.commons.lang3.StringUtils;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

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
			ref, e.getAspect()
			+ (StringUtils.isNotBlank(e.getAspectDescription())
			? " (" + e.getAspectDescription() + ")"
			: "")
			+ ": " + e.getMessage());
	}

	static int statusValue(Severity s) {
		if (s == null)
			return RefStatus.INFO;
		return switch (s) {
			case ERROR -> RefStatus.ERROR;
			case SUCCESS -> RefStatus.OK;
			case WARNING -> RefStatus.WARNING;
			default -> RefStatus.INFO;
		};
	}

	private static DataSetType type(DatasetType iType) {
		if (iType == null)
			return null;
		return switch (iType) {
			case CONTACT -> DataSetType.CONTACT;
			case EXTERNAL_FILE -> DataSetType.EXTERNAL_FILE;
			case FLOW -> DataSetType.FLOW;
			case FLOWPROPERTY -> DataSetType.FLOW_PROPERTY;
			case LCIAMETHOD -> DataSetType.LCIA_METHOD;
			case PROCESS -> DataSetType.PROCESS;
			case SOURCE -> DataSetType.SOURCE;
			case UNITGROUP -> DataSetType.UNIT_GROUP;
			default -> null;
		};
	}

}
