package eu.bcvsolutions.idm.core.api.exception;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Operation was not processed, but accepted to next processing / approving etc.
 * 
 * @author Radek Tomiška
 */
public class AcceptedException extends ResultCodeException {

	private static final long serialVersionUID = 8618305141694647413L;

	public AcceptedException() {
		super(CoreResultCode.ACCEPTED);
	}
}
