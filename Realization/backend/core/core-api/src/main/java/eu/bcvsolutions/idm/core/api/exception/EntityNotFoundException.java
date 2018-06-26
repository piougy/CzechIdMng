package eu.bcvsolutions.idm.core.api.exception;

import java.io.Serializable;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Entity not found exception
 * 
 * @author Radek Tomiška
 *
 */
public class EntityNotFoundException extends ResultCodeException {

	private static final long serialVersionUID = 1L;
	//
	private final Serializable entityId;
	private final Class<?> entityType;
	
	public EntityNotFoundException(Class<?> entityType, Serializable entityId) {
		super(CoreResultCode.ENTITY_NOT_FOUND, ImmutableMap.of(
				"entityType", entityType == null ? String.valueOf(entityType) : entityType.getCanonicalName(),
				"entityId", String.valueOf(entityId))
				);
		this.entityId = entityId;
		this.entityType = entityType;
	}
	
	public Serializable getEntityId() {
		return entityId;
	}
	
	public Class<?> getEntityType() {
		return entityType;
	}
	
}
