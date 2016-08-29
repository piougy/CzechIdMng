package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;

/**
 * Adding security to IdentityWorkingPosition repository
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
@RepositoryEventHandler(IdmIdentityWorkingPosition.class)
public class IdmIdentityWorkingPositionHandler {	
	
	@HandleBeforeSave
	public void handleBeforeSave(IdmIdentityWorkingPosition identityWorkingPosition) {		
		// nothing, just security
	}	
	
	@HandleBeforeCreate
	public void handleBeforeCreate(IdmIdentityWorkingPosition identityWorkingPosition) {	
		// nothing, just security
	}	
	
	@HandleBeforeDelete
	public void handleBeforeDelete(IdmIdentityWorkingPosition identityWorkingPosition) {	
		// nothing, just security
	}
	
}
