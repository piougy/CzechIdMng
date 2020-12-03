package eu.bcvsolutions.idm.core.security.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Password has to be changed after usage.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public class TwoFactorAuthenticationRequiredException extends ResultCodeException  {

	private static final long serialVersionUID = 1L;
	private final String token;

	public TwoFactorAuthenticationRequiredException(String token) {
		super(CoreResultCode.TWO_FACTOR_AUTH_REQIURED, ImmutableMap.of(
				"token", token
		));
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}
