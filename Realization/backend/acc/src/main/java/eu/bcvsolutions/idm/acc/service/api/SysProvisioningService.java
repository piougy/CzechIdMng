package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.security.domain.GuardedString;

public interface SysProvisioningService {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	void doIdentityProvisioning(IdmIdentity identity);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void deleteAccount(AccAccount account);
	
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
	 * @param idmPropertyName
	 * @param value
	 * @param system
	 * @param operationType
	 * @param entityType
	 */
	void doProvisioningForAttribute(String uid, String idmPropertyName, Object value, SysSystem system, 
			AccountOperationType operationType, SystemEntityType entityType);

	
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
	
}