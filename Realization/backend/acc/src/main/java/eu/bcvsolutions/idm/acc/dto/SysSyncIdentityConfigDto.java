package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Identity sync configuration DTO
 * 
 * @author svandav
 *
 */

@Relation(collectionRelation = "synchronizationConfigs")
public class SysSyncIdentityConfigDto extends AbstractSysSyncConfigDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID defaultRole;

	public UUID getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(UUID defaultRole) {
		this.defaultRole = defaultRole;
	}
}
