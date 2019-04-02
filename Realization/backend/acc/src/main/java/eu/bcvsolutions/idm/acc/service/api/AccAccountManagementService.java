package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;

/**
 * 
 * @author Vít Švanda
 *
 */
public interface AccAccountManagementService {
	
	/**
	 * We needs accounts (IDs) which were connected to deleted identity-role in next processors (we want to execute provisioning only for that accounts).
	 */
	String ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE = "account-ids-for-deleted-identity-role";

	/**
	 * Create or delete accounts for this identity according their roles
	 * @param identity
	 * @return
	 */
	boolean resolveIdentityAccounts(IdmIdentityDto identity);

	/**
	 * Identity role is deleting, we have to delete linked identity accounts
	 * 
	 * @param entity
	 * @return list of accounts IDs (used this identity-role)
	 */
	List<UUID> deleteIdentityAccount(IdmIdentityRoleDto identityRole);
	
	/**
	 * Identity role is deleting, we have to delete linked identity accounts, or mark them for delete
	 * 
	 * @param event
	 */
	void deleteIdentityAccount(EntityEvent<IdmIdentityRoleDto> event);
	
	/**
	 * Return UID for this dto and roleSystem. First, the transform script
	 * from the roleSystem attribute is found and used. If UID attribute
	 * for the roleSystem is not defined, then default UID attribute handling
	 * will be used.
	 * 
	 * @param dto
	 * @param roleSystem
	 * @return
	 */
	String generateUID(AbstractDto dto, SysRoleSystemDto roleSystem);

	/**
	 * Create new identity-accounts and accounts for given identity-roles
	 * 
	 * @param identity
	 * @param identityRoles
	 * @return List account's IDs for modified by this action (for this accounts provisioning should be executed).
	 */
	List<UUID> resolveNewIdentityRoles(IdmIdentityDto identity, IdmIdentityRoleDto... identityRoles);

	/**
	 * Create or delete identity-accounts and accounts for given identity-roles
	 * 
	 * @param identity
	 * @param identityRoles
	 * @return List account's IDs for modified by this action (for this accounts provisioning should be executed).
	 */
	List<UUID> resolveUpdatedIdentityRoles(IdmIdentityDto identity, IdmIdentityRoleDto... identityRoles);
}
