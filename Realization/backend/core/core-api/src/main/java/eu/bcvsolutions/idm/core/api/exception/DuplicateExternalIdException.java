package eu.bcvsolutions.idm.core.api.exception;

import java.io.Serializable;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Unique external identifier ex
 * 
 * @author Radek Tomiška
 *
 */
public class DuplicateExternalIdException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	private final String externalId;
	private final Serializable duplicateId;
	
	public DuplicateExternalIdException(String entityType, String externalId, Serializable duplicateId) {
		super(CoreResultCode.DUPLICATE_EXTERNAL_ID, ImmutableMap.of(
				"entityType", String.valueOf(entityType),
				"externalId", String.valueOf(externalId),
				"duplicateId", String.valueOf(duplicateId)
				));
		this.entityType = entityType;
		this.externalId = externalId;
		this.duplicateId = duplicateId;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public String getExternalId() {
		return externalId;
	}
	
	public Serializable getDuplicateId() {
		return duplicateId;
	}
}
