package eu.bcvsolutions.idm.core.api.exception;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Password cannot be changed.
 * 
 * @author Radek Tomiška
 * @since 11.0.0
 */
public class PasswordChangeException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	
	public PasswordChangeException(ResultCode resultCode, Map<String, Object> parameters) {
		super(new DefaultErrorModel(resultCode, parameters));
	}
	
	public PasswordChangeException(LocalDate till) {
		this(CoreResultCode.PASSWORD_CANNOT_CHANGE, ImmutableMap.of("date", till));
	}
}
