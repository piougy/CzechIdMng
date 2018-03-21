package eu.bcvsolutions.idm.core.api.exception;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Operation was not processed, but accepted to next processing / approving etc.
 * 
 * @author Radek Tomiška
 */
public class AcceptedException extends ResultCodeException {

	private static final long serialVersionUID = 8618305141694647413L;
	/**
	 * It's typically identifier of the request.
	 */
	private String identifier;

	public AcceptedException() {
		super(CoreResultCode.ACCEPTED);
	}

	/**
	 * AcceptedException
	 * 
	 * @param identifier - It's typically identifier of the request.
	 */
	public AcceptedException(String identifier) {
		super(CoreResultCode.ACCEPTED);
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}
}
