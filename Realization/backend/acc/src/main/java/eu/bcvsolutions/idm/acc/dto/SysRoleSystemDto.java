package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Role could assign account on target system (account template) DTO.
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "roleSystems")
public class SysRoleSystemDto extends AbstractDto implements Requestable {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@Embedded(dtoClass = SysSystemMappingDto.class)
	private UUID systemMapping;
	private boolean forwardAccountManagemen = false;
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity
	@Embedded(dtoClass = IdmRequestDto.class)
	private UUID request; // Isn't persist in the entity

	public UUID getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(UUID systemMapping) {
		this.systemMapping = systemMapping;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public boolean isForwardAccountManagemen() {
		return forwardAccountManagemen;
	}

	public void setForwardAccountManagemen(boolean forwardAccountManagemen) {
		this.forwardAccountManagemen = forwardAccountManagemen;
	}

	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}

	@Override
	public UUID getRequest() {
		return request;
	}

	@Override
	public void setRequest(UUID request) {
		this.request = request;
	}
}
