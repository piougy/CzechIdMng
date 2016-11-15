package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
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
	
}