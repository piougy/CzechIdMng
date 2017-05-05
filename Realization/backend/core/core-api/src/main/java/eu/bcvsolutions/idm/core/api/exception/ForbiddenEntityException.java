package eu.bcvsolutions.idm.core.api.exception;

import java.io.Serializable;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;

/**
 * Simple 403 exeption
 * 
 * @author Radek Tomiška
 */
public class ForbiddenEntityException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	
	private final Serializable entityId;	
	
	public ForbiddenEntityException(Serializable entityId) {
		super(CoreResultCode.FORBIDDEN, entityId != null ? ImmutableMap.of("entity", entityId) : null);
		this.entityId = entityId;
	}
	
	public Serializable getEntityId() {
		return entityId;
	}
}
