package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;

/**
 * Persists role catalogue items.
 * 
 * @author Radek Tomiška
 */
@Component
@Description("Persists role catalogue items.")
public class RoleCatalogueSaveProcessor extends CoreEventProcessor<IdmRoleCatalogueDto> {

	private static final String PROCESSOR_NAME = "role-catalogue-save-processor";
	@Autowired private IdmRoleCatalogueService service;
	
	public RoleCatalogueSaveProcessor() {
		super(RoleCatalogueEventType.CREATE, RoleCatalogueEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCatalogueDto> process(EntityEvent<IdmRoleCatalogueDto> event) {
		IdmRoleCatalogueDto dto = event.getContent();
		dto = service.saveInternal(dto);
		event.setContent(dto);
		//
		return new DefaultEventResult<>(event, this);
	}

}
