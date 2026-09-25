package app.editors.connection;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.io.SodaConnection;

public class AuthStateTest {

	@Test
	public void testOf() {
		Assert.assertEquals(AuthState.NONE, AuthState.of(null));

		var con = new SodaConnection();
		Assert.assertEquals(AuthState.NONE, AuthState.of(con));

		// a username without credentials is anonymous access
		con.user = "user";
		Assert.assertEquals(AuthState.NONE, AuthState.of(con));

		// a password needs a username
		con.password = "password";
		Assert.assertEquals(AuthState.PASSWORD, AuthState.of(con));

		// a token wins over a password
		con.token = "token";
		Assert.assertEquals(AuthState.TOKEN, AuthState.of(con));

		// credentials without a username are ignored
		con.user = null;
		Assert.assertEquals(AuthState.NONE, AuthState.of(con));

		con.user = "user";
		con.password = null;
		Assert.assertEquals(AuthState.TOKEN, AuthState.of(con));

		// blank values are ignored
		con.user = "  ";
		Assert.assertEquals(AuthState.NONE, AuthState.of(con));

		con.user = "user";
		con.token = "  ";
		con.password = "password";
		Assert.assertEquals(AuthState.PASSWORD, AuthState.of(con));
	}
}
