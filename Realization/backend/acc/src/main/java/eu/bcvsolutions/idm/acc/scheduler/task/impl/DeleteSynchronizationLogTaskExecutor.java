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

import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Delete synchronization logs.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@DisallowConcurrentExecution
@Component(DeleteSynchronizationLogTaskExecutor.TASK_NAME)
public class DeleteSynchronizationLogTaskExecutor
		extends AbstractSchedulableStatefulExecutor<SysSyncLogDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteSynchronizationLogTaskExecutor.class);
	public static final String TASK_NAME = "acc-delete-synchronization-log-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // archive older than
	public static final String PARAMETER_SYSTEM = "system"; // system
	public static final int DEFAULT_NUMBER_OF_DAYS = 180;
	//
	@Autowired private SysSyncLogService service;
	//
	private int numberOfDays = 0; // optional
	private UUID systemId = null; // optional
	
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
		systemId = getParameterConverter().toEntityUuid(properties, PARAMETER_SYSTEM, SysSystemDto.class);
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting synchronization logs older than [{}] days with system [{}].",
				numberOfDays, systemId);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting synchronization logs older than [{}] days  with system [{}]. Processed logs [{}].", 
				numberOfDays, systemId, counter);
		return result;
	}
	
	@Override
	public Page<SysSyncLogDto> getItemsToProcess(Pageable pageable) {
		SysSyncLogFilter filter = new SysSyncLogFilter();
		filter.setSystemId(systemId);
		filter.setRunning(Boolean.FALSE);
		if (numberOfDays > 0) {
			filter.setTill(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(numberOfDays));
		}
		return service.find(filter, PageRequest.of(0, pageable.getPageSize())); // new pageable is given => records are deleted and we need the first page all time
	}

	@Override
	public Optional<OperationResult> processItem(SysSyncLogDto dto) {
		service.delete(dto);
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		parameters.add(PARAMETER_SYSTEM);
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		properties.put(PARAMETER_SYSTEM, systemId);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		//
		IdmFormAttributeDto system = new IdmFormAttributeDto(
				PARAMETER_SYSTEM,
				"System", 
				PersistentType.UUID);
		system.setFaceType(AccFaceType.SYSTEM_SELECT);
		//
		return Lists.newArrayList(numberOfDaysAttribute, system);
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
