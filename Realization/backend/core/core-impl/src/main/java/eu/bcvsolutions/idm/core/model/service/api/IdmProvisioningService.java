package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public interface IdmProvisioningService {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	void doProvisioning(IdmIdentity identity);
	
	/**
	 * TODO: Change only for selected accounts, now is password changed on all accounts
	 * 
	 * Change password for selected identity accounts.
	 * @param identity
	 * @param passwordChange
	 */
	void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange);
}