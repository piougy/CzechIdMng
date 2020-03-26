package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.event.IdentityProjectionEvent.IdentityProjectionEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.IdentityProjectionProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.IdentityProjectionManager;

/**
 * Persists identity projection.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component(IdentityProjectionSaveProcessor.PROCESSOR_NAME)
@Description("Persists identity projection.")
public class IdentityProjectionSaveProcessor
		extends CoreEventProcessor<IdmIdentityProjectionDto> 
		implements IdentityProjectionProcessor {

	public static final String PROCESSOR_NAME = "core-identity-projection-save-processor";
	//
	@Autowired private IdentityProjectionManager manager;
	
	public IdentityProjectionSaveProcessor() {
		super(IdentityProjectionEventType.UPDATE, IdentityProjectionEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityProjectionDto> process(EntityEvent<IdmIdentityProjectionDto> event) {
		IdmIdentityProjectionDto dto = manager.saveInternal(event, event.getPermission());
		event.setContent(dto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
