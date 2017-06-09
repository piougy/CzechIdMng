package eu.bcvsolutions.idm.core.api.domain;

import javax.xml.bind.annotation.XmlTransient;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Database constraint - error codes mapping
 * 
 * @author Radek Tomiška
 */
public enum ConstraintResultCode implements ResultCode {
	
	ROLE_DELETE_FAILED_IDENTITY_ASSIGNED("fk_idm_identity_role_role", "Role (%s) cannot be deleted - some identites have role assigned.");
	
	private final String constraintName;
	private final String message;
	
	private ConstraintResultCode(String constraintName, String message) {
		this.constraintName = constraintName;
		this.message = message;
	}

	@Override
	public String getCode() {
		return constraintName;
	}
	
	/**
	 * We dont want expose implementation details - ignore in json and xml
     *
	 * @return
	 */
	@XmlTransient
	@JsonIgnore
	public String getConstraintName() {
		return this.name();
	}

	@Override
	public String getModule() {
		return "core";
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.CONFLICT;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
