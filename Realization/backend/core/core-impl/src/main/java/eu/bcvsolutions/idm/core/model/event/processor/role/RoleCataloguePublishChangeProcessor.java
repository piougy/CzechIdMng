package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;

/**
 * Publish role catalogue change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish role catalogue change event.")
public class RoleCataloguePublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmRoleCatalogueDto> {

	public static final String PROCESSOR_NAME = "role-catalogue-publish-change-processor";
	
	public RoleCataloguePublishChangeProcessor() {
		super(RoleCatalogueEventType.CREATE, RoleCatalogueEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
