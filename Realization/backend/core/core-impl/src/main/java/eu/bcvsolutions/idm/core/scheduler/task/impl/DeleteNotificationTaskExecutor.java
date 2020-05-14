package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog_;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Delete notifications.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@DisallowConcurrentExecution
@Component(DeleteNotificationTaskExecutor.TASK_NAME)
public class DeleteNotificationTaskExecutor
		extends AbstractSchedulableStatefulExecutor<IdmNotificationLogDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteNotificationTaskExecutor.class);
	public static final String TASK_NAME = "core-delete-notification-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // events older than
	public static final String PARAMETER_SENT_ONLY = "sentOnly"; // sent notification
	//
	public static final int DEFAULT_NUMBER_OF_DAYS = 180; // half year by default
	public static final boolean DEFAULT_SENT_ONLY = true;
	//
	@Autowired private IdmNotificationLogService service;
	//
	private int numberOfDays = 0; // optional
	private boolean sentOnly; // optional
	
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
		sentOnly = getParameterConverter().toBoolean(properties, PARAMETER_SENT_ONLY, DEFAULT_SENT_ONLY);
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting notifications older than [{}] days [sentOnly: {}].", numberOfDays, sentOnly);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting notifications older than [{}] days [sent only: {}]. Processed notifications [{}].",
				numberOfDays, sentOnly, counter);
		return result;
	}
	
	@Override
	public Page<IdmNotificationLogDto> getItemsToProcess(Pageable pageable) {
		IdmNotificationFilter filter = new IdmNotificationFilter();
		if (sentOnly) {
			filter.setState(NotificationState.ALL);
			filter.setSent(Boolean.TRUE);
		}
		if (numberOfDays > 0) {
			filter.setTill(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(numberOfDays));
		}
		return service.find(
				filter, 
				PageRequest.of(0, pageable.getPageSize(), new Sort(Direction.ASC, IdmNotificationLog_.parent.getName()))
				); // new pageable is given => records are deleted and we need the first page all time
	}

	@Override
	public Optional<OperationResult> processItem(IdmNotificationLogDto dto) {
		if (service.get(dto) != null) { // child notification can be deleted before.
			service.delete(dto);
		}
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		parameters.add(PARAMETER_SENT_ONLY);
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		properties.put(PARAMETER_SENT_ONLY, sentOnly);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		//
		IdmFormAttributeDto sentAttribute = new IdmFormAttributeDto(PARAMETER_SENT_ONLY, PARAMETER_SENT_ONLY, PersistentType.BOOLEAN);
		sentAttribute.setDefaultValue(String.valueOf(DEFAULT_SENT_ONLY));
		//
		return Lists.newArrayList(numberOfDaysAttribute, sentAttribute);
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
