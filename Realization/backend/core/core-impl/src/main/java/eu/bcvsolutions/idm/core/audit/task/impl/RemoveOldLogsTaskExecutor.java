package eu.bcvsolutions.idm.core.audit.task.impl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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
 * Remove {@link IdmLoggingEventDto}, {@link IdmLoggingEventExceptionDto} and {@link IdmLoggingEventPropertyDto}.
 *
 * TODO: rename to DeleteLogTaskExecutor
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@DisallowConcurrentExecution
@Component(RemoveOldLogsTaskExecutor.TASK_NAME)
public class RemoveOldLogsTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveOldLogsTaskExecutor.class);
	//
	public static final String TASK_NAME = "core-remove-old-logs-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "removeRecordOlderThan";
	public static final int DEFAULT_NUMBER_OF_DAYS = 90;

	@Autowired
	private IdmLoggingEventService loggingEventService;
	//
	private int numberOfDays;

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
	}

	@Override
	protected boolean start() {
		LOG.warn("Start deleting logs older than [{}] days.", numberOfDays);
		//
		return super.start();
	}

	@Override
	protected Boolean end(Boolean result, Exception ex) {
		LOG.warn("End deleting logs older than [{}]. Processed logs [{}].",
				numberOfDays, counter);
		//
		return super.end(result, ex);
	}

	@Override
	public Boolean process() {
		ZonedDateTime dateTimeTill = ZonedDateTime.now().minusDays(numberOfDays);
		//
		IdmLoggingEventFilter filter = new IdmLoggingEventFilter();
		filter.setTill(dateTimeTill);

		// only for get total elements
		Page<IdmLoggingEventDto> loggingEvents = loggingEventService.find(
				filter, PageRequest.of(0, 1, Sort.by(IdmLoggingEvent_.timestmp.getName())));
		//
		this.count = loggingEvents.getTotalElements();
		this.setCounter(0l);
		this.updateState();
		//
		int deletedItems = loggingEventService.deleteLowerOrEqualTimestamp(dateTimeTill.toInstant().toEpochMilli());
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
