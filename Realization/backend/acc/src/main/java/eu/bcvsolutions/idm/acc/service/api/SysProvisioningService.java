package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

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
	 * TODO: Change only for selected accounts, now is password changed on all accounts
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
	
}