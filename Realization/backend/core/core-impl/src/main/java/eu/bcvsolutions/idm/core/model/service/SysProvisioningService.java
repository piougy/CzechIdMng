package eu.bcvsolutions.idm.core.model.service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public interface SysProvisioningService {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	public void doIdentityProvisioning(IdmIdentity identity);
}