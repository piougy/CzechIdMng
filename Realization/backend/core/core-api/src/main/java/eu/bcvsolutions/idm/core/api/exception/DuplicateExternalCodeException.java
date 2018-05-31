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
public class DuplicateExternalCodeException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	private final String externalCode;
	private final Serializable duplicateId;
	
	public DuplicateExternalCodeException(String entityType, String externalCode, Serializable duplicateId) {
		super(CoreResultCode.DUPLICATE_EXTERNAL_CODE, ImmutableMap.of(
				"entityType", String.valueOf(entityType),
				"externalCode", String.valueOf(externalCode),
				"duplicateId", String.valueOf(duplicateId)
				));
		this.entityType = entityType;
		this.externalCode = externalCode;
		this.duplicateId = duplicateId;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public String getExternalCode() {
		return externalCode;
	}
	
	public Serializable getDuplicateId() {
		return duplicateId;
	}
}
