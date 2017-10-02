package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.event.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Synchronization start event processor
 * @author svandav
 *
 */
@Component
@Description("Starts synchronization process by given configuration")
public class SynchronizationStartProcessor extends AbstractEntityEventProcessor<SysSyncConfigDto> {

	public static final String PROCESSOR_NAME = "synchronization-start-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SynchronizationStartProcessor.class);
	private final SynchronizationService synchronizationService;
	
	@Autowired
	public SynchronizationStartProcessor(
			SynchronizationService synchronizationService) {
		super(SynchronizationEventType.START);
		//
		Assert.notNull(synchronizationService);
		//
		this.synchronizationService = synchronizationService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<SysSyncConfigDto> process(EntityEvent<SysSyncConfigDto> event) {
		LOG.info("Synchronization event start");
		SysSyncConfigDto config = event.getContent();
		synchronizationService.startSynchronization(config);
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}