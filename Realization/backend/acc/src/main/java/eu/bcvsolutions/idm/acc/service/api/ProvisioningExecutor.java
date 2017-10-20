package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.config.domain.ProvisioningConfiguration;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

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
	 * it to the queue. If system's using queue, operation will be processed asynchronously.
	 * 
	 * @param provisioningOperation executed operation
	 */
	void execute(SysProvisioningOperationDto provisioningOperation);
	
	/**
	 * Execute operation synchronously without queue and waiting to transaction ends.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	SysProvisioningOperationDto executeSync(SysProvisioningOperationDto provisioningOperation);
	
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
	 * @return last processed request result (previous requests will be OperationState.EXECUTED)
	 */
	OperationResult execute(SysProvisioningBatchDto batch);
	
	/**
	 * Cancel operations in batch.
	 * 
	 * @param batch executed batch
	 */
	void cancel(SysProvisioningBatchDto batch);
	
	/**
	 * Returns configuration for provisioning
	 * 
	 * @return
	 */
	ProvisioningConfiguration getConfiguration();
}
