package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Disableable;

/**
 * Given entity type is not disableable.
 * 
 * @see Disableable
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class EntityTypeNotDisableableException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	
	public EntityTypeNotDisableableException(String entityType) {
		this(entityType, null);
	}
	
	public EntityTypeNotDisableableException(String entityType, Exception ex) {
		super(CoreResultCode.ENTITY_TYPE_NOT_EXTERNAL_CODEABLE, ImmutableMap.of(
				"entityType", String.valueOf(entityType)
				), ex);
		this.entityType = entityType;
	}
	
	public String getEntityType() {
		return entityType;
	}

}
