package epd.io;

import org.openlca.ilcd.commons.Ref;

public class RefStatus {

	public static final int CANCEL = 1;
	public static final int ERROR = 2;
	public static final int INFO = 4;
	public static final int OK = 8;
	public static final int WARNING = 16;

	public final int severity;
	public final Ref ref;
	public final String message;

	public RefStatus(int severity, Ref ref, String message) {
		this.severity = severity;
		this.ref = ref;
		this.message = message;
	}
}
