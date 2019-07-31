package eu.bcvsolutions.idm.rpt.api.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 * 
 * @author Radek Tomiška
 */
public enum RptResultCode implements ResultCode {
	
	REPORT_GENERATE_SUCCESS(HttpStatus.OK, "Report [%s] was successfully completed."),
	REPORT_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Report [%s] generation failed."),
	REPORT_RENDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Report [%s] rendering failed."),
	REPORT_WRONG_DEFINITION(HttpStatus.BAD_REQUEST, "Report type [%s] is wrong. Should be [%s]."),
	REPORT_NO_FORM_ATTRIBUTE(HttpStatus.BAD_REQUEST, "No form attribute with name [%s] found!");
	
	
	private final HttpStatus status;
	private final String message;
	
	private RptResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}
	
	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return RptModuleDescriptor.MODULE_ID;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
