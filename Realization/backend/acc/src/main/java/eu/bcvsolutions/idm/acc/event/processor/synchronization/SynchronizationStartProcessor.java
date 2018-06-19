package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.event.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * Synchronization start event processor
 * @author svandav
 *
 */
@Component
@Description("Starts synchronization process by given configuration")
public class SynchronizationStartProcessor extends AbstractEntityEventProcessor<AbstractSysSyncConfigDto> {

	public static final String PROCESSOR_NAME = "synchronization-start-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SynchronizationStartProcessor.class);
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	
	public SynchronizationStartProcessor() {
		super(SynchronizationEventType.START);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<AbstractSysSyncConfigDto> process(EntityEvent<AbstractSysSyncConfigDto> event) {
		LOG.info("Synchronization event start");
		AbstractSysSyncConfigDto config = event.getContent();
		Assert.notNull(config);
		Assert.notNull(config.getId(), "Id of sync config is required!");
		SynchronizationSchedulableTaskExecutor lrt = new SynchronizationSchedulableTaskExecutor(config.getId());
		longRunningTaskManager.execute(lrt);
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}