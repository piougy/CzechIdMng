package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleCompositionProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent.RoleCompositionEventType;

/**
 * Persists role composition
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component(RoleCompositionSaveProcessor.PROCESSOR_NAME)
@Description("Persists role composition.")
public class RoleCompositionSaveProcessor
		extends CoreEventProcessor<IdmRoleCompositionDto> 
		implements RoleCompositionProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-composition-save-processor";
	//
	@Autowired private IdmRoleCompositionService service;
	
	public RoleCompositionSaveProcessor() {
		super(RoleCompositionEventType.CREATE); // update is not supported
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCompositionDto> process(EntityEvent<IdmRoleCompositionDto> event) {
		IdmRoleCompositionDto roleComposition = event.getContent();
		roleComposition = service.saveInternal(roleComposition);
		event.setContent(roleComposition);
		//
		return new DefaultEventResult<>(event, this);
	}
}
