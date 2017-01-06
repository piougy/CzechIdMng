package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;

/**
 * Entry point to all provisioning operations.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 *
 */
public interface ProvisioningExecutor {

	/**
	 * Executes the resource operation. If the operation fails, it creates a resource request and saves
	 * it to the queue.
	 */
	void executeOperation(SysProvisioningOperation provisioningOperation);
}
