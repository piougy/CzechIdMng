package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.MappingAttribute;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
public interface SysProvisioningService {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	void doProvisioning(IdmIdentity identity);
	
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
	void doProvisioning(AccAccount account, IdmIdentity identity);

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
	void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange);
	
	/**
	 * Do provisioning only for single attribute. For example, it is needed to change password
	 * @param uid
	 * @param mappedAttribute
	 * @param value
	 * @param system
	 * @param operationType
	 * @param entityType
	 * @param entity
	 */
	void doProvisioningForAttribute(String uid, MappingAttribute mappedAttribute, Object value, SysSystem system,
			AccountOperationType operationType, SystemEntityType entityType, AbstractEntity entity);
	
	
	/**
	 * Do authenticate check for given identityAccount on target resource. Username for check is get from account (attribute UID) linked in identityAccount.
	 * @param identityAccount
	 * @param system
	 * @return
	 */
	IcfUidAttribute authenticate(AccIdentityAccount identityAccount, SysSystem system);
	
	/**
	 * Do authenticate check for given username and password on target resource
	 * @param username
	 * @param password
	 * @param system
	 * @param operationType
	 * @param entityType
	 * @return
	 */
	IcfUidAttribute authenticate(String username, GuardedString password, SysSystem system,
			SystemOperationType operationType, SystemEntityType entityType);

	/**
	 * Convert method for SysRoleSystemAttribute to mapping attribute dto
	 * @param overloadingAttribute
	 * @param overloadedAttribute
	 */
	void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute, MappingAttribute overloadedAttribute);

	
}