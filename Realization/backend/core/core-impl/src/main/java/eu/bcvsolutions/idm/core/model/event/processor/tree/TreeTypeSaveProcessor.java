package eu.bcvsolutions.idm.core.model.event.processor.tree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent.TreeTypeEventType;

/**
 * Persists tree type.
 * 
 * @author Radek Tomiška
 */
@Component
@Description("Persists tree type.")
public class TreeTypeSaveProcessor extends CoreEventProcessor<IdmTreeTypeDto> {

	private static final String PROCESSOR_NAME = "tree-type-save-processor";
	@Autowired private IdmTreeTypeService service;
	
	public TreeTypeSaveProcessor() {
		super(TreeTypeEventType.CREATE, TreeTypeEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeTypeDto> process(EntityEvent<IdmTreeTypeDto> event) {
		IdmTreeTypeDto dto = event.getContent();
		dto = service.saveInternal(dto);
		event.setContent(dto);
		//
		return new DefaultEventResult<>(event, this);
	}

}
