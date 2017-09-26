package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Basic interface for do provisioning
 * 
 * @author svandav
 *
 */
public interface ProvisioningService {
	
	static final String PASSWORD_SCHEMA_PROPERTY_NAME = "__PASSWORD__";
	
	/**
	 * Provisioned dto (identity, role ...)
	 */
	static final String DTO_PROPERTY_NAME = "dto";
	
	/**
	 * Property in provisioning start event. If is value TRUE, then will be cancelled provisioning break during account protection.
	 * In extra cases, we do provisioning with account in protection. For example we need do first provisioning (for move account to archive) 
	 */
	static final String CANCEL_PROVISIONING_BREAK_IN_PROTECTION = "cancel_provisioning_break_in_account_protection";
	
	/**
	 * Property in event. If is value TRUE, then will be provisioning skipped. Skip must be implemented in every processor for now!
	 */
	static final String SKIP_PROVISIONING = "skip_provisioning";

	/**
	 * Do provisioning for given content (dto) on all connected systems
	 * 
	 * @param dto
	 */
	void doProvisioning(AbstractDto dto);
	
	/**
	 * Do provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doProvisioning(AccAccountDto account);
	
	/**
	 * Do provisioning for given account and content (dto)
	 * Emits ProvisioningEventType.START event.
	 * 
	 * @param account
	 * @param dto
	 * @return
	 */
	void doProvisioning(AccAccountDto account, AbstractDto dto);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 * @param entityType
	 * @param entityId - Id of entity connected to the account. Can be null, but provisioning archive will not have correct information.
	 * 
	 */
	void doDeleteProvisioning(AccAccountDto account, SystemEntityType entityType, UUID entityId);
	
	/**
	 * 
	 * Change password for selected dto's accounts.
	 * 
	 * @param dto
	 * @param passwordChange
	 * @return result for each provisioned account
	 */
	List<OperationResult> changePassword(AbstractDto dto, PasswordChangeDto passwordChange);
	
	/**
	 * Do provisioning only for single attribute. For example, it is needed to change password
	 * 
	 * @param systemEntity
	 * @param mappedAttribute
	 * @param value
	 * @param system
	 * @param operationType
	 * @param dto
	 * @return result
	 */
	OperationResult doProvisioningForAttribute(SysSystemEntityDto systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, AbstractDto dto);
	
	/**
	 * Do authenticate check for given username and password on target resource
	 * 
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
	 * @param uid
	 * @param account
	 * @param dto
	 * @param system
	 * @param entityType
	 * @return
	 */
	List<AttributeMapping> resolveMappedAttributes(AccAccountDto account, AbstractDto dto, SysSystemDto system, SystemEntityType entityType);

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
	 * Create accounts for given content (dto) on all systems with provisioning mapping and same entity type.
	 * 
	 * @param entity
	 * @param entityType
	 */
	void createAccountsForAllSystems(AbstractDto dto);
	
	/**
	 * Do provisioning for given account and dto. For internal purpose without emit event.
	 * 
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doInternalProvisioning(AccAccountDto account, AbstractDto dto);
	
}
