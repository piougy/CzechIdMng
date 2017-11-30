package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with IdmIdentity
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityService extends 
		EventableDtoService<IdmIdentityDto, IdmIdentityFilter>,
		AuthorizableService<IdmIdentityDto>,
		CodeableService<IdmIdentityDto>,
		ScriptEnabled {

	/**
	 * Returns identity by given username
	 * @param username
	 * @return
	 */
	IdmIdentityDto getByUsername(String username);

	/**
	 * Better "toString".
	 * Returns identity's fullName with titles if lastName is not blank, otherwise returns username 
	 * 
	 * @param identity
	 * @return
	 */
	String getNiceLabel(IdmIdentityDto identity);
	
	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 * @return - change on accounts
	 */
	List<OperationResult> passwordChange(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto);
	
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param roleId
	 * @return List of identities with assigned role
	 */
	List<IdmIdentityDto> findAllByRole(UUID roleId);
	
	/**
	 * Find all identities by assigned role name
	 * 
	 * @param roleName
	 * @return List of identities with assigned role
	 */
	List<IdmIdentityDto> findAllByRoleName(String roleName);
	

	/**
	 * Method finds all identity's managers by identity contract (guarantee or by assigned tree structure).
	 * If no manager is found, then identities wit admin role (by configuration) is returned.
	 * 
	 * @param forIdentity
	 * @param byTreeType If optional tree type is given, then only managers defined with this type is returned
	 * @return
	 */
	List<IdmIdentityDto> findAllManagers(UUID forIdentity, UUID byTreeType);

	/**
	 * Method finds all identity's managers by identity contract and return managers
	 * 
	 * @param forIdentity
	 * @return String - usernames separate by commas
	 */
	List<IdmIdentityDto> findAllManagers(UUID forIdentity);

	/**
	 * Contains list of identities some identity with given username.
	 * If yes, then return true.
	 * @param identities
	 * @param username
	 * @return
	 */
	@Beta
	boolean containsUser(List<IdmIdentityDto> identities, String username);

	/**
	 * Convert given identities to string of user names separate with comma 
	 * 
	 * @param identities
	 * @return
	 */
	@Beta
	String convertIdentitiesToString(List<IdmIdentityDto> identities);

	/**
	 * Find all guarantees for given role ID
	 * 
	 * @param roleId
	 * @return
	 */
	@Beta
	List<IdmIdentityDto> findAllGuaranteesByRoleId(UUID roleId);

	
	/**
	 * Update IdmAuthorityChange for all given identities and set 
	 * it to provided value. 
	 * 
	 * @param identities identities to update
	 * @param changeTime change time to set
	 */
	@Beta
	void updateAuthorityChange(List<UUID> identities, DateTime changeTime);
	
	/**
	 * Method create new IdentityEvent for pre validate password
	 * 
	 * @param passwordChange
	 * @param identity
	 */
	void validate(PasswordChangeDto passwordChange, IdmIdentityDto identity);
}
