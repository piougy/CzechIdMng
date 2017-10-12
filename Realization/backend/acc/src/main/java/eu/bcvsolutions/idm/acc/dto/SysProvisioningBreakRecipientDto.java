package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakRecipient;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * DTO for {@link SysProvisioningBreakRecipient}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "provisioningBreakRecipients")
public class SysProvisioningBreakRecipientDto extends AbstractDto {

	private static final long serialVersionUID = 4789214107870098278L;

	@Embedded(dtoClass = SysProvisioningBreakConfigDto.class)
	private UUID breakConfig;
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;

	public UUID getBreakConfig() {
		return breakConfig;
	}

	public void setBreakConfig(UUID breakConfig) {
		this.breakConfig = breakConfig;
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}
}
