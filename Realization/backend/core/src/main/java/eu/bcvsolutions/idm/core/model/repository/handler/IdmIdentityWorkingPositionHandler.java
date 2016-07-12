package eu.bcvsolutions.idm.core.model.repository.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Adding security to IdentityWorkingPosition repository
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
@RepositoryEventHandler(IdmIdentityWorkingPosition.class)
public class IdmIdentityWorkingPositionHandler {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmIdentityWorkingPositionHandler.class);
	private static final String ROLE_MANAGER = "manager"; // TODO: configurable
	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	@Autowired
	private IdmRoleRepository roleRepository;
	
	
	@HandleBeforeSave
	public void handleBeforeSave(IdmIdentityWorkingPosition identityWorkingPosition) {		
		assignManagerRole(identityWorkingPosition);
	}	
	
	@HandleBeforeCreate
	public void handleBeforeCreate(IdmIdentityWorkingPosition identityWorkingPosition) {	
		assignManagerRole(identityWorkingPosition);
	}	
	
	@HandleBeforeDelete
	public void handleBefore(IdmIdentityWorkingPosition identityWorkingPosition) {	
		// nothing, just security
	}
	
	private void assignManagerRole(IdmIdentityWorkingPosition identityWorkingPosition) {
		if (identityWorkingPosition.getManager() == null) {
			log.debug("Position does not manager defined");
			return;
		}
		IdmRole managerRole = roleRepository.findOneByName(ROLE_MANAGER);
		if (managerRole == null) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND,  "Role ["+ROLE_MANAGER+"] not found", ImmutableMap.of("role", ROLE_MANAGER));
		}
		List<IdmIdentityRole> existIdentityRoles = identityRoleRepository.findAllByIdentityAndRole(identityWorkingPosition.getManager(), managerRole);
		if(existIdentityRoles.isEmpty()) {
			// TODO: run as
			IdmIdentityRole managerIdentityRole = new IdmIdentityRole();
			managerIdentityRole.setIdentity(identityWorkingPosition.getManager());
			managerIdentityRole.setRole(managerRole);
			identityRoleRepository.save(managerIdentityRole);
			log.debug("Position [{}] has manager defined. Role [{}] automatically added to identity [{}]", identityWorkingPosition, ROLE_MANAGER, identityWorkingPosition.getManager());
		}
	}
	
}
