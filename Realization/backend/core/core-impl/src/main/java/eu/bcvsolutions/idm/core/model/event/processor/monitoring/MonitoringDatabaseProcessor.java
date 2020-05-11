package eu.bcvsolutions.idm.core.model.event.processor.monitoring;

import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractMonitoringDatabaseProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.model.event.MonitoringEvent;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import java.text.MessageFormat;

/**
 * Monitoring of core database processor (count).
 *
 * @author Vít Švanda
 *
 */
@Component(MonitoringDatabaseProcessor.PROCESSOR_NAME)
@Description("Monitoring of core database processor (count).")
public class MonitoringDatabaseProcessor
		extends AbstractMonitoringDatabaseProcessor<IdmMonitoringTypeDto> {

	public static final String PROCESSOR_NAME = "monitoring-database-processor";
	public static final String WORKFLOW_HIST_TABLE_NAME = "act_hi_procinst";

	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmEntityStateService entityStateService;
	@Autowired
	private IdmEntityEventService entityEventService;
	@Autowired
	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;

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
		monitoringType.setModule(getModule());

		monitoringType.getResults().add(countToResult(identityRoleService));
		monitoringType.getResults().add(countToResult(roleRequestService));
		monitoringType.getResults().add(countToResult(entityEventService));
		monitoringType.getResults().add(countToResult(entityStateService));
		IdmMonitoringResultDto workflowCountMonitoring = countToResult(workflowHistoricProcessInstanceService);
		workflowCountMonitoring.setName(MessageFormat.format("{0} ({1})", getDtoName(workflowHistoricProcessInstanceService), WORKFLOW_HIST_TABLE_NAME));
		monitoringType.getResults().add(workflowCountMonitoring);

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
		return 100;
	}
}
