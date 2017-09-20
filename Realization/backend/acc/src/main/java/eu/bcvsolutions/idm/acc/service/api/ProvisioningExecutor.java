package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;

/**
 * Entry point to all provisioning operations.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 *
 */
public interface ProvisioningExecutor {

	/**
	 *  Executes the resource operation. If the operation fails, it creates a resource request and saves
	 * it to the queue.
	 * 
	 * @param provisioningOperation executed operation
	 * @return
	 */
	SysProvisioningOperationDto execute(SysProvisioningOperationDto provisioningOperation);
	
	/**
	 * Cancel the resource operation.
	 * 
	 * @param provisioningOperation executed operation
	 */
	SysProvisioningOperationDto cancel(SysProvisioningOperationDto provisioningOperation);
	
	/**
	 *  Executes operations in given batch.
	 * 
	 * @param batch executed batch
	 * @return
	 */
	void execute(SysProvisioningBatchDto batch);
	
	/**
	 * Cancel operations in batch.
	 * 
	 * @param batch executed batch
	 */
	void cancel(SysProvisioningBatchDto batch);
}
