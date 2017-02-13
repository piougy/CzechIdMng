package eu.bcvsolutions.idm.ic.exception;

/**
 * Default exception for remote server connections
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IcRemoteServerException extends IcException {

	private static final long serialVersionUID = 1L;
	
	private String host;
	
	private int port;
	
	public IcRemoteServerException (String host, int port, Throwable cause) {
		super(cause);
		//
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
