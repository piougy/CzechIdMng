package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Persists provisioning operation batches
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningBatchService extends ReadWriteEntityService<SysProvisioningBatch, EmptyFilter> {

	/**
	 * Finds batch for given operation.
	 * 
	 * @param operation
	 * @return
	 */
	SysProvisioningBatch findBatch(SysProvisioningOperation operation);
}
