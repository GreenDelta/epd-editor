package epd.io;

public class ServerConfig {

	public ServerCredentials uploadCredentials;
	public ServerCredentials downloadCredentials;

	@Override
	protected ServerConfig clone() {
		ServerConfig clone = new ServerConfig();
		if (downloadCredentials != null)
			clone.downloadCredentials = downloadCredentials.clone();
		if (uploadCredentials != null)
			clone.uploadCredentials = uploadCredentials.clone();
		return clone;
	}

	@Override
	public String toString() {
		return "ServerConfig [ download=" + downloadCredentials
				+ ", upload=" + uploadCredentials + "]";
	}

}
