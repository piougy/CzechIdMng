package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Provisioning preparation
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@RepositoryEventHandler(IdmRole.class)
public class IdmRoleEventHandler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmRoleEventHandler.class);

	@HandleBeforeSave
	public void handleBeforeSave(IdmRole role) {		
		log.debug("1 Role [{}] will be saved", role);
	}
	
	@HandleBeforeCreate
	public void handleBeforeCreate(IdmRole role) {		
		log.debug("1 Role [{}] will be created", role);
	}	
}
