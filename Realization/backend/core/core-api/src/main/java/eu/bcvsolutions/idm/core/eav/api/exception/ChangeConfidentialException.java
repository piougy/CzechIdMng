package eu.bcvsolutions.idm.core.eav.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Confidential flag for form attribute cannot be changed - some form values already using this attribute. 
 * Data migrations are not implemented.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class ChangeConfidentialException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String attributeCode;
	
	public ChangeConfidentialException(String attributeCode) {
		this(attributeCode, null);
	}
	
	public ChangeConfidentialException(String attributeCode, Exception ex) {
		super(CoreResultCode.FORM_ATTRIBUTE_CHANGE_CONFIDENTIAL_FAILED_HAS_VALUES, ImmutableMap.of(
				"attributeCode", String.valueOf(attributeCode)
				), ex);
		this.attributeCode = attributeCode;
	}
	
	public String getAttributeCode() {
		return attributeCode;
	}

}
