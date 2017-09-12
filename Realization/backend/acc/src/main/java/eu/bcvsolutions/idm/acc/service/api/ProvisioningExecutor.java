package eu.bcvsolutions.idm.acc.service.api;

import org.springframework.transaction.event.TransactionalEventListener;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
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
	 *  Executes the resource operation. If the operation fails, it creates a resource request and saves
	 * it to the queue.
	 * 
	 * @param provisioningOperation executed operation
	 * @return
	 */
	SysProvisioningOperation execute(SysProvisioningOperation provisioningOperation);
	
	/**
	 * We need to wait to transaction commit, when provisioning is executed - all accounts have to be prepared.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	@Beta
	@TransactionalEventListener
	SysProvisioningOperation executeInternal(SysProvisioningOperation provisioningOperation);
	
	/**
	 * Cancel the resource operation.
	 * 
	 * @param provisioningOperation executed operation
	 */
	SysProvisioningOperation cancel(SysProvisioningOperation provisioningOperation);
	
	/**
	 *  Executes operations in given batch.
	 * 
	 * @param batch executed batch
	 * @return
	 */
	void execute(SysProvisioningBatch batch);
	
	/**
	 * Cancel operations in batch.
	 * 
	 * @param batch executed batch
	 */
	void cancel(SysProvisioningBatch batch);
}
