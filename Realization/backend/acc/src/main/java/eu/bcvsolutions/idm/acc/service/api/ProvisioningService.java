package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Basic interface for do provisioning
 * 
 * @author svandav
 *
 */
public interface ProvisioningService<ENTITY> {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	void doProvisioning(ENTITY identity);
	
	/**
	 * Do provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doProvisioning(AccAccount account);
	
	/**
	 * Do provisioning for given account and identity
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doProvisioning(AccAccount account, ENTITY identity);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doDeleteProvisioning(AccAccount account);
	
	/**
	 * 
	 * Change password for selected identity accounts.
	 * @param identity
	 * @param passwordChange
	 */
	void changePassword(ENTITY identity, PasswordChangeDto passwordChange);
	
	/**
	 * Do provisioning only for single attribute. For example, it is needed to change password
	 * 
	 * @param systemEntity
	 * @param mappedAttribute
	 * @param value
	 * @param system
	 * @param operationType
	 * @param entity
	 */
	void doProvisioningForAttribute(SysSystemEntity systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, ENTITY entity);
	
	/**
	 * Do authenticate check for given username and password on target resource
	 * @param username
	 * @param password
	 * @param system
	 * @param entityType
	 * @return
	 */
	IcUidAttribute authenticate(String username, GuardedString password, SysSystem system, SystemEntityType entityType);

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param uid
	 * @param account
	 * @param identity
	 * @param system
	 * @param entityType
	 * @return
	 */
	List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, ENTITY entity, SysSystem system, SystemEntityType entityType);

	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes);

	/**
	 * Create accounts for given entity on all systems with provisioning mapping and same entity type.
	 * @param entity
	 * @param entityType
	 */
	void createAccountsForAllSystems(ENTITY entity);

	
}