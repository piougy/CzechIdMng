package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventFilter;
import eu.bcvsolutions.idm.core.audit.service.api.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.audit.service.api.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Long running task for remove old record from event logging tables.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@DisallowConcurrentExecution
@Description("Removes old logs from event logging tables.")
public class RemoveOldLogsTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveOldLogsTaskExecutor.class);
	
	@Autowired private IdmLoggingEventService loggingEventService;
	@Autowired private IdmLoggingEventExceptionService loggingEventExceptionService;
	
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
		//
		LoggingEventFilter filter = new LoggingEventFilter();
		filter.setTill(DateTime.now().minusDays(days.intValue()));
		List<IdmLoggingEventDto> loggingEvents = loggingEventService.find(filter, null).getContent();
		Long exceptionCounter = 0l;
		//
		this.counter = (long) loggingEvents.size();
		for (IdmLoggingEventDto event : loggingEvents) {
			LOG.debug("Event id: [{}] will be removed", event.getId());
			List<IdmLoggingEventExceptionDto> exceptions = loggingEventExceptionService.findAllByEvent(Long.valueOf(event.getId().toString()), null).getContent();
			if (!exceptions.isEmpty()) {
				exceptionCounter += exceptions.size();
				LOG.debug("For event id: [{}], found [{}] exceptions, remove this exceptions.", event.getId(), exceptions.size());
				loggingEventExceptionService.deleteAll(exceptions);
			}
			loggingEventService.delete(event);
			this.count++;
		}
		LOG.info("Removed logs older than [{}] days was successfully completed. Removed logs: [{}] and their exceptions [{}].", days, this.counter, exceptionCounter);
		//
		return Boolean.TRUE;
	}

	@Override
	public List<String> getParameterNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_DAYS);
		return parameters;
	}
}
