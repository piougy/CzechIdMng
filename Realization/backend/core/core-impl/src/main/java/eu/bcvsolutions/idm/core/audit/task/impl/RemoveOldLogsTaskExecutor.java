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

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventPropertyDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent_;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Long running task for remove old record from event logging tables.
 * Remove {@link IdmLoggingEventDto}, {@link IdmLoggingEventExceptionDto} and {@link IdmLoggingEventPropertyDto},
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@DisallowConcurrentExecution
@Description("Removes old logs from event logging tables (events, eventException and eventProperty).")
public class RemoveOldLogsTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveOldLogsTaskExecutor.class);
	
	@Autowired
	private IdmLoggingEventService loggingEventService;
	
	private String PARAMETER_DAYS = "removeRecordOlderThan";
	private Long days;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		days = getParameterConverter().toLong(properties, PARAMETER_DAYS);
	}
	
	@Override
	public Boolean process() {
		if (days == null) {
			LOG.warn("Parameter {} is not filled. This task will be skipped.", PARAMETER_DAYS);
			return Boolean.TRUE;
		}
		DateTime dateTimeTill = DateTime.now().minusDays(days.intValue());
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
		LOG.info("Removed logs older than [{}] days was successfully completed. Removed logs [{}].", days, this.counter);
		//
		return Boolean.TRUE;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_DAYS);
		return parameters;
	}
}
