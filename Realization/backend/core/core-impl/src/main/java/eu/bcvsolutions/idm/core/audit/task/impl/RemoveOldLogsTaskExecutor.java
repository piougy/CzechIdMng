package eu.bcvsolutions.idm.core.audit.task.impl;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventPropertyDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent_;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Long running task for remove old record from event logging tables.
 * Remove {@link IdmLoggingEventDto}, {@link IdmLoggingEventExceptionDto} and {@link IdmLoggingEventPropertyDto},
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service(RemoveOldLogsTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Removes old logs from event logging tables (events, eventException and eventProperty).")
public class RemoveOldLogsTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveOldLogsTaskExecutor.class);
	//
	public static final String TASK_NAME = "core-remove-old-logs-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "removeRecordOlderThan";
	public static final int DEFAULT_NUMBER_OF_DAYS = 90;
	
	@Autowired
	private IdmLoggingEventService loggingEventService;
	//
	private Long numberOfDays;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		numberOfDays = getParameterConverter().toLong(properties, PARAMETER_NUMBER_OF_DAYS);
	}
	
	@Override
	public Boolean process() {
		if (numberOfDays == null) {
			LOG.warn("Parameter {} is not filled. This task will be skipped.", PARAMETER_NUMBER_OF_DAYS);
			return Boolean.TRUE;
		}
		DateTime dateTimeTill = DateTime.now().minusDays(numberOfDays.intValue());
		//
		IdmLoggingEventFilter filter = new IdmLoggingEventFilter();
		filter.setTill(dateTimeTill);
		
		// only for get total elements
		Page<IdmLoggingEventDto> loggingEvents = loggingEventService.find(
				filter, new PageRequest(0, 1, new Sort(IdmLoggingEvent_.timestmp.getName())));
		//
		this.count = loggingEvents.getTotalElements();
		this.setCounter(0l);
		this.updateState();
		//
		int deletedItems = loggingEventService.deleteLowerOrEqualTimestamp(dateTimeTill.getMillis());
		this.setCounter(Long.valueOf(deletedItems));
		//
		LOG.info("Removed logs older than [{}] days was successfully completed. Removed logs [{}].", numberOfDays, this.counter);
		//
		return Boolean.TRUE;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(
				PARAMETER_NUMBER_OF_DAYS, 
				PARAMETER_NUMBER_OF_DAYS, 
				PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		//
		return Lists.newArrayList(numberOfDaysAttribute);
	}
}
