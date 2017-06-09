package eu.bcvsolutions.idm.ic.exception;

/**
 * Default ic exception for remote connector serve, UNEXPECTED error
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IcCantConnectException extends IcRemoteServerException {

	public IcCantConnectException(String host, int port, Throwable cause) {
		super(host, port, cause);
	}

	private static final long serialVersionUID = 1L;

}
