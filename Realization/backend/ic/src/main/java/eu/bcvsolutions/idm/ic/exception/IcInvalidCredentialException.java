package eu.bcvsolutions.idm.ic.exception;

/**
 * Ic exception for invalid credential. Exception can be throw when
 * connect system to remote connector server
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IcInvalidCredentialException extends IcRemoteServerException {
	
	public IcInvalidCredentialException(String host, int port, Throwable cause) {
		super(host, port, cause);
	}

	private static final long serialVersionUID = 1L;
}
