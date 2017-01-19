package eu.bcvsolutions.idm.acc.service.api;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Persists provisioning operation batches
 * 
 * @author Radek Tomiška
 * @author Filip Mestanek
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
	
	/**
	 * Calculates when the request should be invoked
	 * 
	 * @return Date of the next attempt. Null if there should be no next attempt 
	 */
	DateTime calculateNextAttempt(SysProvisioningRequest request);
}
