package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningOperationService extends 
		ReadWriteDtoService<SysProvisioningOperationDto, SysProvisioningOperationFilter>,
		AuthorizableService<SysProvisioningOperationDto>,
		ScriptEnabled {

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
	 * Saves operation in new transaction
	 * 
	 * @param operation
	 * @return
	 * @since 9.6.0
	 */
	SysProvisioningOperationDto saveOperation(SysProvisioningOperationDto operation);
	
	/**
	 * Deletes operation in new transaction
	 * 
	 * @param operation
	 * @since 9.6.0 
	 */
	void deleteOperation(SysProvisioningOperationDto operation);
	
	/**
	 * Handles failed operation (plans next attempt etc.).
	 * Operation state is saved in new transaction @since 9.6.0. Don't forget to save operation states in new transaction too if needed.
	 * 
	 * @param operation
	 * @param ex
	 * @return
	 * @see #saveOperation(SysProvisioningOperationDto)
	 */
	SysProvisioningOperationDto handleFailed(SysProvisioningOperationDto operation, Exception ex);
	
	/**
	 * Called when operation succeeded. 
	 * Operation state is saved in new transaction @since 9.6.0. Don't forget to save operation states in new transaction too if needed.
	 * 
	 * @param operation
	 * @return
	 * @see #saveOperation(SysProvisioningOperationDto)
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
	
	/**
	 * Returns {@link SysSystemEntityDto} by given provisioning operation
	 * 
	 * @param operation
	 * @return
	 */
	SysSystemEntityDto getByProvisioningOperation(SysProvisioningOperationDto operation);
	
	/**
	 * Delete all operations for the given system. Archive is not used, delete operations directly.
	 * 
	 * @param systemId
	 * @return
	 * @since 8.1.4
	 */
	long deleteOperations(UUID systemId);

	/**
	 * Delete all operations. Archive is not used, delete operations directly without any audit information.
	 * 
	 * @since 9.5.2
	 */
	void deleteAllOperations();

	/**
	 * Optimize - system can be pre-loaded in DTO.
	 * 
	 * @param operation
	 * @return
	 */
	SysSystemDto getSystem(SysProvisioningOperationDto operation);
}
