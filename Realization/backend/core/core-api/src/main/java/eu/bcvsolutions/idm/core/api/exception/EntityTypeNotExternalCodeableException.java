package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Given entity type is not externally codeable.
 * 
 * @author Radek Tomiška
 *
 */
public class EntityTypeNotExternalCodeableException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	
	public EntityTypeNotExternalCodeableException(String entityType) {
		super(CoreResultCode.ENTITY_TYPE_NOT_EXTERNAL_CODEABLE, ImmutableMap.of(
				"entityType", String.valueOf(entityType)
				));
		this.entityType = entityType;
	}
	
	public String getEntityType() {
		return entityType;
	}

}
