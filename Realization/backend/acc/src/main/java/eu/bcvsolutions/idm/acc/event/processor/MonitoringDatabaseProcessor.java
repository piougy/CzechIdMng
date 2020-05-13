package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractMonitoringDatabaseProcessor;
import eu.bcvsolutions.idm.core.model.event.MonitoringEvent;

/**
 * Monitoring of ACC database processor (count).
 *
 * @author Vít Švanda
 *
 */
@Component(MonitoringDatabaseProcessor.PROCESSOR_NAME)
@Description("Monitoring of ACC database processor (count).")
public class MonitoringDatabaseProcessor
		extends AbstractMonitoringDatabaseProcessor<IdmMonitoringTypeDto> {

	public static final String PROCESSOR_NAME = "acc-monitoring-database-processor";

	@Autowired
	private SysProvisioningOperationService operationService;
	@Autowired
	private SysProvisioningArchiveService archiveService;

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

		monitoringType.getResults().add(countToResult(operationService));
		monitoringType.getResults().add(countToResult(archiveService));

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmMonitoringTypeDto> event) {
		boolean result = super.conditional(event);

		if (!result) {
			return false;
		}
		return MONITORING_TYPE_DATABASE.equals(event.getContent().getType());
	}
	
	@Override
	public int getOrder() {
		return 200;
	}
}
