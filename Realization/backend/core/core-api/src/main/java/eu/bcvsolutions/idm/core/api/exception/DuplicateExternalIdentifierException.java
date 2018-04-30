package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Unique external identifier ex
 * 
 * @author Radek Tomiška
 *
 */
public class DuplicateExternalIdentifierException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String entityType;
	private final String externalId;
	
	public DuplicateExternalIdentifierException(String entityType, String externalId) {
		super(CoreResultCode.DUPLICATE_EXTERNAL_IDENTIFIER, ImmutableMap.of(
				"entityType", String.valueOf(entityType),
				"externalId", String.valueOf(externalId)
				));
		this.entityType = entityType;
		this.externalId = externalId;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public String getExternalId() {
		return externalId;
	}
}
