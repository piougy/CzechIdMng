package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * API for entity provisioning executors
 * @author svandav
 *
 */
public interface ProvisioningEntityExecutor<ENTITY> extends Plugin<SystemEntityType>{

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
	void doProvisioning(AccAccountDto account);
	
	/**
	 * Do provisioning for given account and identity
	 * Emits ProvisioningEventType.START event.
	 * 
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doProvisioning(AccAccountDto account, ENTITY identity);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 * @param entityId - Id of entity connected to the account. Can be null, but provisioning archive will not have correct information.
	 */
	void doDeleteProvisioning(AccAccountDto account, UUID entityId);
	
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
	void doProvisioningForAttribute(SysSystemEntityDto systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, ENTITY entity);
	
	/**
	 * Do authenticate check for given username and password on target resource
	 * @param username
	 * @param password
	 * @param system
	 * @param entityType
	 * @return
	 */
	IcUidAttribute authenticate(String username, GuardedString password, SysSystemDto system, SystemEntityType entityType);

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param account
	 * @param identity
	 * @param system
	 * @param entityType
	 * @return
	 */
	List<AttributeMapping> resolveMappedAttributes(AccAccountDto account, ENTITY entity, SysSystemDto system, SystemEntityType entityType);

	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttributeDto> overloadingAttributes, SystemEntityType entityType);

	/**
	 * Create accounts for given entity on all systems with provisioning mapping and same entity type.
	 * @param entity
	 * @param entityType
	 */
	void createAccountsForAllSystems(ENTITY entity);

	/**
	 * Do provisioning for given account and identity. For internal purpose without emit event.
	 * 
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doInternalProvisioning(AccAccountDto account, ENTITY entity);



}