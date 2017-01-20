package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Operations with IdmIdentity
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityService extends ReadWriteEntityService<IdmIdentity, IdentityFilter>, IdentifiableByNameEntityService<IdmIdentity> {
	
	@Deprecated
	static final String CONFIDENTIAL_PROPERTY_PASSWORD = "password";
	
	static final String ADD_ROLE_TO_IDENTITY_WORKFLOW = "changeIdentityRoles";
	
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
	String findAllByRoleAsString(UUID roleId);
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param role
	 * @return List of IdmIdentity with assigned role
	 */
	List<IdmIdentity> findAllByRole(IdmRole role);

	/**
	 * Method finds all identity's managers by identity contract and return manager's usernames,
	 * 
	 * separated by commas
	 * 
	 * @param id
	 * @return String - usernames separate by commas
	 */
	String findAllManagersAsString(UUID identityId);

	/**
	 * Method finds all identity's managers by identity contract (guarantee or by assigned tree structure).
	 * 
	 * @param forIdentity
	 * @param byTreeType If optional tree type is given, then only managers defined with this type is returned
	 * @return
	 */
	List<IdmIdentity> findAllManagers(IdmIdentity forIdentity, IdmTreeType byTreeType);
}
