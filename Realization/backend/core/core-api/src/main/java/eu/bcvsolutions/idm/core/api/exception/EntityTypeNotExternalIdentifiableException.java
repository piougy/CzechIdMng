package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Given entity type is not externally identifiable.
 * 
 * @author Radek Tomiška
 *
 */
public class EntityTypeNotExternalIdentifiableException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	
	public EntityTypeNotExternalIdentifiableException(String entityType) {
		this(entityType, null);
	}
	
	public EntityTypeNotExternalIdentifiableException(String entityType, Exception ex) {
		super(CoreResultCode.ENTITY_TYPE_NOT_EXTERNAL_IDENTIFIABLE, ImmutableMap.of(
				"entityType", String.valueOf(entityType)
				), ex);
		this.entityType = entityType;
	}
	
	public String getEntityType() {
		return entityType;
	}

}
