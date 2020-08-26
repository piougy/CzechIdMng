package eu.bcvsolutions.idm.acc.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * System entity not found on target system.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
public class SystemEntityNotFoundException extends ResultCodeException  {

	private static final long serialVersionUID = 1L;
	private final String uid;
	private final String systemCode;

	public SystemEntityNotFoundException(ResultCode resultCode, String uid, String systemCode) {
		super(resultCode, ImmutableMap.of("uid", uid, "system", systemCode));
		this.uid = uid;
		this.systemCode = systemCode;
	}

	public String getUid() {
		return uid;
	}
	
	public String getSystemCode() {
		return systemCode;
	}
}
