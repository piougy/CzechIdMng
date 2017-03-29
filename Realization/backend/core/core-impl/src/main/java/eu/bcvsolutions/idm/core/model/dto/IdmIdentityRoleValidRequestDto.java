package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;

/**
 * DTO for {@link IdmIdentityRoleValidRequest}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmIdentityRoleValidRequestDto extends AbstractDto {
	
	private static final long serialVersionUID = -4256009496017969313L;
	
	@Embedded(dtoClass = IdmIdentityRoleDto.class)
	private UUID identityRole;
	private int currentAttempt;
	private OperationResult result;
	
	public UUID getIdentityRole() {
		return identityRole;
	}
	public int getCurrentAttempt() {
		return currentAttempt;
	}
	public OperationResult getResult() {
		return result;
	}
	public void setIdentityRole(UUID identityRole) {
		this.identityRole = identityRole;
	}
	public void setCurrentAttempt(int currentAttempt) {
		this.currentAttempt = currentAttempt;
	}
	public void setResult(OperationResult result) {
		this.result = result;
	}
	public void increaseAttempt() {
		this.currentAttempt++;
	}
}
