package eu.bcvsolutions.idm.core.api.exception;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.Codeable;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.modelmapper.internal.util.Assert;

/**
 * Simple 403 exeption
 * 
 * @author Radek Tomiška
 */
public class ForbiddenEntityException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	
	private final Serializable entityId;
	private final BasePermission[] basePermission;
	
	public ForbiddenEntityException(Serializable entityId, BasePermission... basePermission) {
		super(CoreResultCode.FORBIDDEN_ENTITY, ImmutableMap.of(
				"entity", String.valueOf(entityId),
				"permission", basePermission == null ? "null" : StringUtils.join(basePermission, ", ")
				));
		this.entityId = entityId;
		this.basePermission = basePermission;
	}
	
	public ForbiddenEntityException(BaseEntity entity, BasePermission... basePermission) {
		super((entity instanceof Codeable) ? CoreResultCode.FORBIDDEN_CODEABLE_ENTITY : CoreResultCode.FORBIDDEN_ENTITY, ImmutableMap.of(
				"entity", entity != null ? String.valueOf(entity.getId()) : "null",
				"permission", basePermission == null ? "null" : StringUtils.join(basePermission, ", "),
				"type", entity != null ? entity.getClass().getSimpleName() : "null",
				"code", entity instanceof Codeable ? ((Codeable) entity).getCode() : "null"
		));
		this.entityId = entity != null ? entity.getId() : null;
		this.basePermission = basePermission;
	}
	
	public ForbiddenEntityException(BaseDto dto, BasePermission... basePermission) {
		super((dto instanceof Codeable) ? CoreResultCode.FORBIDDEN_CODEABLE_ENTITY : CoreResultCode.FORBIDDEN_ENTITY, ImmutableMap.of(
				"entity", dto != null ? String.valueOf(dto.getId()) : "null",
				"permission", basePermission == null ? "null" : StringUtils.join(basePermission, ", "),
				"type", dto != null ? dto.getClass().getSimpleName() : "null",
				"code", dto instanceof Codeable ? ((Codeable) dto).getCode() : "null"
		));
		this.entityId = dto != null ? dto.getId() : null;
		this.basePermission = basePermission;
	}
	
	public Serializable getEntityId() {
		return entityId;
	}
	
	public BasePermission[] getBasePermission() {
		return basePermission;
	}
	
	
}
