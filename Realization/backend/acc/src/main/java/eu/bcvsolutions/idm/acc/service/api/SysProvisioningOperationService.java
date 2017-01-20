package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningOperationService extends ReadWriteEntityService<SysProvisioningOperation, EmptyFilter> {

	/**
	 * Handles failed operation (plans next attempt etc.)
	 * 
	 * @param operation
	 * @param ex
	 */
	void handleFailed(SysProvisioningOperation operation, Exception ex);
	
	/**
	 * Called when operation succeeded. 
	 * 
	 * @param operation
	 */
	void handleSuccessful(SysProvisioningOperation operation);
}
