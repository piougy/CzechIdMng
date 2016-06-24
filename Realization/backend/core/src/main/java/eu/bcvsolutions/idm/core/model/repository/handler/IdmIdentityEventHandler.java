package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Provisioning preparation
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@RepositoryEventHandler(IdmIdentity.class)
public class IdmIdentityEventHandler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmIdentityEventHandler.class);

	@HandleBeforeSave
	public void handleBeforeSave(IdmIdentity identity) {		
		log.debug("Identity [{}] will be saved", identity);
	}	
}
