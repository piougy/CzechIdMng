package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningOperationService extends ReadWriteDtoService<SysProvisioningOperationDto, SysProvisioningOperationFilter> {

	/**
	 * Returns fully loaded AccountObject with guarded string.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	Map<ProvisioningAttributeDto, Object> getFullAccountObject(SysProvisioningOperationDto provisioningOperation);
	
	/**
	 * Returns fully loaded ConnectorObject with guarded strings.
	 * 
	 * TODO: don't update connectorObject in provisioningOperation (needs attribute defensive clone)
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	IcConnectorObject getFullConnectorObject(SysProvisioningOperationDto provisioningOperation);
	
	/**
	 * Handles failed operation (plans next attempt etc.)
	 * 
	 * @param operation
	 * @param ex
	 * @return
	 */
	SysProvisioningOperationDto handleFailed(SysProvisioningOperationDto operation, Exception ex);
	
	/**
	 * Called when operation succeeded. 
	 * 
	 * @param operation
	 * @return
	 */
	SysProvisioningOperationDto handleSuccessful(SysProvisioningOperationDto operation);
	
	/**
	 * Creates account object property key into confidential storage
	 * 
	 * @param property
	 * @param index
	 * @return
	 */
	String createAccountObjectPropertyKey(String property, int index);
	
	/**
	 * Creates connector object property key into confidential storage
	 * 
	 * @param property
	 * @param index
	 * @return
	 */
	String createConnectorObjectPropertyKey(IcAttribute property, int index);
	
	/**
	 * Return opertaions for batch id.
	 * 
	 * @param batchId
	 * @return
	 */
	Page<SysProvisioningOperationDto> findByBatchId(UUID batchId, Pageable pageable);

	/**
	 * Returns operations for batch id sorted by oldest to newest.
	 * 
	 * @param batchId
	 * @return
	 */
	List<SysProvisioningOperationDto> getByTimelineAndBatchId(UUID batchId);

	/**
	 * Method returns oldest operation for batch id.
	 * 
	 * @param batchId
	 * @return
	 */
	SysProvisioningOperationDto getFirstOperationByBatchId(UUID batchId);

	/**
	 * Method return newest operation by batch id.
	 * 
	 * @param batchId
	 * @return
	 */
	SysProvisioningOperationDto getLastOperationByBatchId(UUID batchId);
}
