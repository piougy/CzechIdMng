package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Delete archived provisioning operations.
 * 
 * @author Radek Tomiška
 * @since 9.6.3
 */
@DisallowConcurrentExecution
@Component(DeleteProvisioningArchiveTaskExecutor.TASK_NAME)
public class DeleteProvisioningArchiveTaskExecutor
		extends AbstractSchedulableStatefulExecutor<SysProvisioningArchiveDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteProvisioningArchiveTaskExecutor.class);
	public static final String TASK_NAME = "acc-delete-provisioning-archive-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // archive older than
	public static final String PARAMETER_OPERATION_STATE = "operationState"; // archive state
	public static final String PARAMETER_SYSTEM = "system"; // system
	public static final String PARAMETER_EMPTY_PROVISIONING = SysProvisioningOperationFilter.PARAMETER_EMPTY_PROVISIONING; // empty provisioning
	public static final int DEFAULT_NUMBER_OF_DAYS = 90;
	public static final OperationState DEFAULT_OPERATION_STATE = OperationState.EXECUTED;
	//
	@Autowired private SysProvisioningArchiveService service;
	//
	private int numberOfDays = 0; // optional
	private OperationState operationState; // optional
	private UUID systemId = null;
	private Boolean emptyProvisioning = null;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		Long givenNumberOfDays = getParameterConverter().toLong(properties, PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			numberOfDays = Math.toIntExact(givenNumberOfDays);
		} else {
			numberOfDays = 0;
		}
		operationState = getParameterConverter().toEnum(properties, PARAMETER_OPERATION_STATE, OperationState.class);
		systemId = getParameterConverter().toEntityUuid(properties, PARAMETER_SYSTEM, SysSystemDto.class);
		emptyProvisioning = getParameterConverter().toBoolean(properties, PARAMETER_EMPTY_PROVISIONING);
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting empty [{}] archived provisioning operations older than [{}] days in state [{}] with system [{}].", 
				emptyProvisioning, numberOfDays, operationState, systemId);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting empty [{}] archived provisioning operations older than [{}] days in state [{}] with system [{}]. Processed operations [{}].", 
				emptyProvisioning, numberOfDays, operationState, systemId, counter);
		return result;
	}
	
	@Override
	public Page<SysProvisioningArchiveDto> getItemsToProcess(Pageable pageable) {
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setResultState(operationState);
		filter.setSystemId(systemId);
		filter.setEmptyProvisioning(emptyProvisioning);
		if (numberOfDays > 0) {
			filter.setTill(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(numberOfDays));
		}
		return service.find(filter, PageRequest.of(0, pageable.getPageSize())); // new pageable is given => records are deleted and we need the first page all time
	}

	@Override
	public Optional<OperationResult> processItem(SysProvisioningArchiveDto dto) {
		service.delete(dto);
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		parameters.add(PARAMETER_OPERATION_STATE);
		parameters.add(PARAMETER_SYSTEM);
		parameters.add(PARAMETER_EMPTY_PROVISIONING);
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		properties.put(PARAMETER_OPERATION_STATE, operationState);
		properties.put(PARAMETER_SYSTEM, systemId);
		properties.put(PARAMETER_EMPTY_PROVISIONING, emptyProvisioning);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		//
		IdmFormAttributeDto operationStateAttribute = new IdmFormAttributeDto(PARAMETER_OPERATION_STATE, PARAMETER_OPERATION_STATE, PersistentType.ENUMERATION);
		operationStateAttribute.setDefaultValue(DEFAULT_OPERATION_STATE.name());
		operationStateAttribute.setFaceType(BaseFaceType.OPERATION_STATE_ENUM);
		//
		IdmFormAttributeDto system = new IdmFormAttributeDto(
				PARAMETER_SYSTEM,
				"System", 
				PersistentType.UUID);
		system.setFaceType(AccFaceType.SYSTEM_SELECT);
		IdmFormAttributeDto emptyProvisioningAttribute = new IdmFormAttributeDto(
				PARAMETER_EMPTY_PROVISIONING,
				PARAMETER_EMPTY_PROVISIONING, 
				PersistentType.BOOLEAN,
				BaseFaceType.BOOLEAN_SELECT);
		emptyProvisioningAttribute.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(numberOfDaysAttribute, operationStateAttribute, system, emptyProvisioningAttribute);
	}
	
    @Override
    public boolean supportsDryRun() {
    	return false; // TODO: get context (or LRT) in getItems to process ...
    }
    
    @Override
	public boolean requireNewTransaction() {
		return true;
	}
    
    @Override
	public boolean supportsQueue() {
		return false;
	}
    
    @Override
    public boolean isRecoverable() {
    	return true;
    }
}
