package eu.bcvsolutions.idm.vs.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractMonitoringDatabaseProcessor;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.model.event.MonitoringEvent;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

/**
 * Monitoring of VS database processor (count).
 *
 * @author Vít Švanda
 *
 */
@Component(MonitoringDatabaseProcessor.PROCESSOR_NAME)
@Description("Monitoring of VS database processor (count).")
public class MonitoringDatabaseProcessor
		extends AbstractMonitoringDatabaseProcessor<IdmMonitoringTypeDto>  {

	public static final String PROCESSOR_NAME = "vs-monitoring-database-processor";

	@Autowired
	private VsRequestService requestService;
	@Autowired
	private VsAccountService accountService;

	public MonitoringDatabaseProcessor() {
		super(MonitoringEvent.MonitoringEventType.CHECK);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmMonitoringTypeDto> process(EntityEvent<IdmMonitoringTypeDto> event) {
		IdmMonitoringTypeDto monitoringType = event.getContent();

		monitoringType.getResults().add(countToResult(requestService));
		monitoringType.getResults().add(countToResult(accountService));

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmMonitoringTypeDto> event) {
		boolean result = super.conditional(event);

		if (!result) {
			return false;
		}
		return MonitoringManager.MONITORING_TYPE_DATABASE.equals(event.getContent().getType());
	}
	
	@Override
	public int getOrder() {
		return 300;
	}
}
