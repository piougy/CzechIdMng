package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Given property has wrong type.
 * 
 * @author svandav
 *
 */
public class CorrelationPropertyUnsupportedTypeException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	private final String property;
	
	public CorrelationPropertyUnsupportedTypeException(String entityType,  String property) {
		this(entityType, property, null);
	}
	
	public CorrelationPropertyUnsupportedTypeException(String entityType,  String property, Exception ex) {
		super(CoreResultCode.CORRELATION_PROPERTY_WRONG_TYPE, ImmutableMap.of(
				"entityType", entityType, "property", property
				), ex);
		this.entityType = entityType;
		this.property = property;
	}
	
	public String getEntityType() {
		return entityType;
	}

	public String getProperty() {
		return property;
	}

}
