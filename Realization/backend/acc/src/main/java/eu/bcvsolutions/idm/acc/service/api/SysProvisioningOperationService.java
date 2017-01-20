package eu.bcvsolutions.idm.acc.service.api;

import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningOperationService extends ReadWriteEntityService<SysProvisioningOperation, EmptyFilter> {

	/**
	 * Returns fully loaded AccountObject with guarded string.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	Map<UUID, Object> getFullAccountObject(SysProvisioningOperation provisioningOperation);
	
	/**
	 * Returns fully loaded ConnectorObject with guarded strings.
	 * 
	 * TODO: don't update connectorObject in provisioningOperation (needs attribute defensive clone)
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	IcConnectorObject getFullConnectorObject(SysProvisioningOperation provisioningOperation);
	
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
