package eu.bcvsolutions.idm.core.api.exception;

import java.io.Serializable;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Simple 403 exeption
 * 
 * @author Radek Tomiška
 */
public class ForbiddenEntityException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	
	private final Serializable entityId;
	private final BasePermission basePermission;
	
	public ForbiddenEntityException(Serializable entityId) {
		this(entityId, null);
	}
	
	public ForbiddenEntityException(Serializable entityId, BasePermission basePermission) {
		super(CoreResultCode.FORBIDDEN_ENTITY, ImmutableMap.of(
				"entity", String.valueOf(entityId),
				"permission", basePermission == null ? String.valueOf(basePermission) : basePermission.getName()
				));
		this.entityId = entityId;
		this.basePermission = basePermission;
	}
	
	public Serializable getEntityId() {
		return entityId;
	}
	
	public BasePermission getBasePermission() {
		return basePermission;
	}
}
