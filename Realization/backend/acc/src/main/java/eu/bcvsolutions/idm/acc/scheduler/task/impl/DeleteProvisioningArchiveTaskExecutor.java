package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Delete archived provisioning operations.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Service
@DisallowConcurrentExecution
@Description("Delete archived provisioning operations.")
public class DeleteProvisioningArchiveTaskExecutor
		extends AbstractSchedulableStatefulExecutor<SysProvisioningArchiveDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteProvisioningArchiveTaskExecutor.class);
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // archive older than
	public static final String PARAMETER_OPERATION_STATE = "operationState"; // archive state
	public static final String PARAMETER_SYSTEM = "system"; // system
	public static final int DEFAULT_NUMBER_OF_DAYS = 90;
	public static final OperationState DEFAULT_OPERATION_STATE = OperationState.EXECUTED;
	//
	@Autowired private SysProvisioningArchiveService service;
	//
	private int numberOfDays = 0; // optional
	private OperationState operationState; // optional
	private UUID systemId = null;
	
	
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
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting archived provisioning operations older than [{}] days in state [{}].", numberOfDays, operationState);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting archived provisioning operations older than [{}] days in state [{}]. Processed operations [{}].", numberOfDays, operationState, counter);
		return result;
	}
	
	@Override
	public boolean isInProcessedQueue(SysProvisioningArchiveDto dto) {
		// we want to log items, but we want to execute them every times
		return false;
	}
	
	@Override
	public Page<SysProvisioningArchiveDto> getItemsToProcess(Pageable pageable) {
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setResultState(operationState);
		filter.setSystemId(systemId);
		if (numberOfDays > 0) {
			filter.setTill(DateTime.now().withTimeAtStartOfDay().minusDays(numberOfDays));
		}
		return service.find(filter, null); // pageable is not given => records are deleted and wee ned the fist page all time
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
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		properties.put(PARAMETER_OPERATION_STATE, operationState);
		properties.put(PARAMETER_SYSTEM, systemId);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		IdmFormAttributeDto operationStateAttribute = new IdmFormAttributeDto(PARAMETER_OPERATION_STATE, PARAMETER_OPERATION_STATE, PersistentType.SHORTTEXT);
		operationStateAttribute.setDefaultValue(DEFAULT_OPERATION_STATE.name());
		// TODO: enumeration state in forms
		IdmFormAttributeDto system = new IdmFormAttributeDto(
				PARAMETER_SYSTEM,
				"System", 
				PersistentType.UUID);
		system.setFaceType(AccFaceType.SYSTEM_SELECT);
		//
		return Lists.newArrayList(numberOfDaysAttribute, operationStateAttribute, system);
	}
	
    @Override
    public boolean supportsDryRun() {
    	return false; // TODO: get context (or LRT) in getItems to process ...
    }
}
