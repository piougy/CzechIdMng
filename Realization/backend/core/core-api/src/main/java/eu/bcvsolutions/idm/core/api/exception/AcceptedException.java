package eu.bcvsolutions.idm.core.api.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;

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
	 * All accepted exeption should generalize AcceptedException.
	 * 
	 * @param resultCode specific accepted result code
	 * @param parameters additional exception paramters
	 * @since 10.4.8
	 */
	public AcceptedException(ResultCode resultCode, Map<String, Object> parameters) {
		super(new DefaultErrorModel(resultCode == null ? CoreResultCode.ACCEPTED : resultCode, parameters));
		//
		if (resultCode != null && resultCode.getStatus() != HttpStatus.ACCEPTED) {
			throw new IllegalArgumentException(
					String.format("Accepted exception supports ACCEPTED status only, given [%s].", resultCode.getStatus())
			);
		}
	}

	/**
	 * AcceptedException
	 * 
	 * @param identifier - It's typically identifier of the request.
	 */
	public AcceptedException(String identifier) {
		super(CoreResultCode.ACCEPTED);
		//
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}
}
