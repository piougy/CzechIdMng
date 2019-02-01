package eu.bcvsolutions.idm.core.eav.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Persistent type for form attribute cannot be changed - some form values already using this attribute. 
 * Data migrations are not implemented.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class ChangePersistentTypeException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String attributeCode;
	
	public ChangePersistentTypeException(String attributeCode) {
		this(attributeCode, null);
	}
	
	public ChangePersistentTypeException(String attributeCode, Exception ex) {
		super(CoreResultCode.FORM_ATTRIBUTE_CHANGE_PERSISTENT_TYPE_FAILED_HAS_VALUES, ImmutableMap.of(
				"attributeCode", String.valueOf(attributeCode)
				), ex);
		this.attributeCode = attributeCode;
	}
	
	public String getAttributeCode() {
		return attributeCode;
	}

}
