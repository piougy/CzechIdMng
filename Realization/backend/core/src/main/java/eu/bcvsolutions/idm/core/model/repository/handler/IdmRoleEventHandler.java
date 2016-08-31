package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * SRole security and provisioning preparation
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@RepositoryEventHandler(IdmRole.class)
public class IdmRoleEventHandler {

	@HandleBeforeSave
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public void handleBeforeSave(IdmRole role) {
		// nothing, just security
	}
	
	@HandleBeforeCreate
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public void handleBeforeCreate(IdmRole role) {		
		// nothing, just security
	}
	
	@HandleBeforeDelete
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_DELETE + "')")
	public void handleBeforeDelete(IdmRole role) {	
		// nothing, just security
	}
}
