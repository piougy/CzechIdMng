package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * When role is assigned to user, then workflow is started if needed
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@RepositoryEventHandler(IdmIdentityRole.class)
public class IdmIdentityRoleEventHandler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmIdentityRoleEventHandler.class);
	
	@HandleBeforeSave
	public void handleBeforeSave(IdmIdentityRole identityRole) {		
		log.warn("IdentityRole [{}] will be saved  (TODO: implemenent change approval ...)", identityRole);
	}
	
	/**
	 * When role is assigned to user, then workflow is started if needed
	 * 
	 * @param identityRole
	 */
//	@HandleBeforeCreate
//	public void handleBeforeCreate(IdmIdentityRole identityRole) {	
//		log.debug("Checking, if role [{}] for identity [{}] has to be approved first", identityRole.getRole(), identityRole.getIdentity());
//		if (identityRole.getRole().isApprovable()) {
//			if (!identityService.addRole(identityRole, true)) {
//				// TODO: better event - it's not "error".
//				throw new RestApplicationException(CoreResultCode.ACCEPTED, "Request was accepted. Role has to be approved first.");
//			}
//		}
//	}
}
