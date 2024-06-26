package app.store.validation;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.events.Severity;
import com.okworx.ilcd.validation.reference.IDatasetReference;

import app.App;
import epd.model.RefStatus;

final class Event {

	private Event() {
	}

	static RefStatus toStatus(IValidationEvent e) {
		if (e == null || e.getReference() == null)
			return null;
		var iRef = e.getReference();
		var ref = new Ref()
				.withType(typeOf(iRef))
				.withUUID(iRef.getUuid())
				.withVersion(iRef.getVersion())
				.withUri(iRef.getUri());
		LangString.set(ref.withName(), iRef.getName(), App.lang());

		return new RefStatus(
				statusValue(e.getSeverity()),
				ref,
				e.getMessage());
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

	private static DataSetType typeOf(IDatasetReference iRef) {
		if (iRef == null || iRef.getType() == null)
			return null;
		return switch (iRef.getType()) {
			case CONTACT -> DataSetType.CONTACT;
			case EXTERNAL_FILE -> DataSetType.EXTERNAL_FILE;
			case FLOW -> DataSetType.FLOW;
			case FLOWPROPERTY -> DataSetType.FLOW_PROPERTY;
			case LCIAMETHOD -> DataSetType.IMPACT_METHOD;
			case PROCESS -> DataSetType.PROCESS;
			case SOURCE -> DataSetType.SOURCE;
			case UNITGROUP -> DataSetType.UNIT_GROUP;
			case LCMODEL -> DataSetType.MODEL;
		};
	}

}
