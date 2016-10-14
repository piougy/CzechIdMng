package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.IdentityFilter;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Operations with IdmIdentity
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityService extends ReadWriteEntityService<IdmIdentity, IdentityFilter>, IdentifiableByNameEntityService<IdmIdentity> {
	
	/**
	 * Returns identity by given username
	 * @param username
	 * @return
	 */
	IdmIdentity getByUsername(String username);

	/**
	 * Better "toString"
	 * 
	 * @param identity
	 * @return
	 */
	String getNiceLabel(IdmIdentity identity);

	/**
	 * Start workflow for change permissions
	 * @param identity
	 * @return
	 */
	ProcessInstance changePermissions(IdmIdentity identity);
	
	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	void passwordChange(IdmIdentity identity, PasswordChangeDto passwordChangeDto);
	
	/**
	 * Find all identities usernames by assigned role
	 * 
	 * @param roleId
	 * @return String with all found usernames separate with comma
	 */
	String findAllByRoleAsString(Long roleId);
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param roleId
	 * @return List of IdmIdentity with assigned role
	 */
	List<IdmIdentity> findAllByRole(Long roleId);

	/**
	 * Method find all managers by user positions and return managers username,
	 * separate by commas
	 * 
	 * @param id
	 * @return String - usernames separate by commas
	 */
	String findAllManagersByUserPositionsString(Long id);

	/**
	 * Method find all managers by user positions and return managers identity
	 * @param id
	 * @return List of IdmIdentities 
	 */
	List<IdmIdentity> findAllManagersByUserPositions(Long id);
}
