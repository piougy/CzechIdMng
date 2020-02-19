package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Delete long running tasks.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@Service(DeleteLongRunningTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Delete long running tasks.")
public class DeleteLongRunningTaskExecutor
		extends AbstractSchedulableStatefulExecutor<IdmLongRunningTaskDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteLongRunningTaskExecutor.class);
	public static final String TASK_NAME = "core-delete-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // events older than
	public static final String PARAMETER_OPERATION_STATE = "operationState"; // archive state
	//
	public static final int DEFAULT_NUMBER_OF_DAYS = 90;
	public static final OperationState DEFAULT_OPERATION_STATE = OperationState.EXECUTED;
	//
	@Autowired private IdmLongRunningTaskService service;
	//
	private int numberOfDays = 0; // optional
	private OperationState operationState; // optional
	
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
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting long running tasks older than [{}] days in state [{}].", numberOfDays, operationState);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting long running tasks older than [{}] days in state [{}]. Processed lrts [{}].",
				numberOfDays, operationState, counter);
		return result;
	}
	
	@Override
	public Page<IdmLongRunningTaskDto> getItemsToProcess(Pageable pageable) {
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setOperationState(operationState);
		filter.setRunning(Boolean.FALSE);
		if (numberOfDays > 0) {
			filter.setTill(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(numberOfDays));
		}
		return service.find(filter, PageRequest.of(0, pageable.getPageSize())); // new pageable is given => records are deleted and we need the first page all time
	}

	@Override
	public Optional<OperationResult> processItem(IdmLongRunningTaskDto dto) {
		service.delete(dto);
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		parameters.add(PARAMETER_OPERATION_STATE);
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		properties.put(PARAMETER_OPERATION_STATE, operationState);
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
		return Lists.newArrayList(numberOfDaysAttribute, operationStateAttribute);
	}
	
	@Override
	public boolean supportsQueue() {
		return false;
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
    public boolean isRecoverable() {
    	return true;
    }
}
