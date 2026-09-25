package app.editors.connection;

import org.openlca.commons.Strings;
import org.openlca.ilcd.io.SodaConnection;

/// The authentication state of a connection.
enum AuthState {

	/// No credentials are stored; the connection uses anonymous access.
	NONE,

	/// A user name and a password are stored.
	PASSWORD,

	/// A user name and an access token are stored.
	TOKEN;

	/// Returns the authentication state of the given connection.
	static AuthState of(SodaConnection con) {
		if (con == null || Strings.isBlank(con.user))
			return NONE;
		if (Strings.isNotBlank(con.token))
			return TOKEN;
		if (Strings.isNotBlank(con.password))
			return PASSWORD;
		return NONE;
	}
}
