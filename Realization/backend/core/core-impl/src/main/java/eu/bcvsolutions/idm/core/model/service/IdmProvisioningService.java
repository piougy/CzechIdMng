package eu.bcvsolutions.idm.core.model.service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public interface IdmProvisioningService {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	void doIdentityProvisioning(IdmIdentity identity);
}