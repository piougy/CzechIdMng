package eu.bcvsolutions.idm.core.security.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Password has to be changed after usage.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
public class MustChangePasswordException extends ResultCodeException  {

	private static final long serialVersionUID = 1L;
	private final String username;

	public MustChangePasswordException(String username) {
		super(CoreResultCode.MUST_CHANGE_IDM_PASSWORD, ImmutableMap.of("user", username));
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

}
