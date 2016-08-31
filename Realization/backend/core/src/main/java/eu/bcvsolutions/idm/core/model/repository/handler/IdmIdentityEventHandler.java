package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Identity security and provisioning preparation
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@RepositoryEventHandler(IdmIdentity.class)
public class IdmIdentityEventHandler {

	@HandleBeforeSave
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public void handleBeforeSave(IdmIdentity identity) {		
		// nothing, just security
	}	
	
	@HandleBeforeCreate
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public void handleBeforeCreate(IdmIdentity identity) {	
		// nothing, just security
	}	
	
	@HandleBeforeDelete
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_DELETE + "')")
	public void handleBeforeDelete(IdmIdentity identity) {	
		// nothing, just security
	}
}
