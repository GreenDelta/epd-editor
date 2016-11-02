package epd.io;

public class ServerCredentials {

	public String url;
	public String user;
	public String password;
	public String dataStockName;
	public String dataStockUuid;

	@Override
	public String toString() {
		return user + "@" + url;
	}

	@Override
	protected ServerCredentials clone() {
		ServerCredentials clone = new ServerCredentials();
		clone.url = url;
		clone.user = user;
		clone.password = password;
		clone.dataStockName = dataStockName;
		clone.dataStockUuid = dataStockUuid;
		return clone;
	}
}
