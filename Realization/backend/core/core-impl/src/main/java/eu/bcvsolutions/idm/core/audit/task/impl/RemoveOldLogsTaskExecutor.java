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
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventPropertyService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent_;
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
	@Autowired private IdmLoggingEventPropertyService loggingEventPropertyService;
	
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
		IdmLoggingEventFilter filter = new IdmLoggingEventFilter();
		filter.setTill(DateTime.now().minusDays(days.intValue()));
		Page<IdmLoggingEventDto> loggingEvents = loggingEventService.find(
				filter, new PageRequest(0, 100, new Sort(IdmLoggingEvent_.timestmp.getName())));
		//
		Long exceptionCounter = 0l;
		boolean canContinue = true;
		this.count = loggingEvents.getTotalElements();
		this.setCounter(0l);
		//
		while (canContinue) {
			for (IdmLoggingEventDto event : loggingEvents) {
				Long eventId = Long.valueOf(event.getId().toString());
				//
				LOG.debug("Event id: [{}] will be removed", event.getId());
				loggingEventExceptionService.deleteByEventId(eventId);
				loggingEventPropertyService.deleteAllByEventId(eventId);
				loggingEventService.deleteAllById(eventId);
				this.increaseCounter();
				//
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
			//
			loggingEvents = loggingEventService.find(filter, new PageRequest(0, 100, new Sort(IdmLoggingEvent_.timestmp.getName())));
			//
			if (loggingEvents.getContent().isEmpty()) {
				break;
			}
		}
		//
		LOG.info("Removed logs older than [{}] days was successfully completed. Removed logs: [{}] and their exceptions [{}].", days, this.counter, exceptionCounter);
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
