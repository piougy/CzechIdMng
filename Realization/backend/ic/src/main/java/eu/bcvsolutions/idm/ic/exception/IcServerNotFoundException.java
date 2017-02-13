package eu.bcvsolutions.idm.ic.exception;

/**
 * Ic exception can be throw when remoe server not found
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IcServerNotFoundException extends IcRemoteServerException{
	
	public IcServerNotFoundException(String host, int port, Throwable cause) {
		super(host, port, cause);
	}

	private static final long serialVersionUID = 1L;
}
