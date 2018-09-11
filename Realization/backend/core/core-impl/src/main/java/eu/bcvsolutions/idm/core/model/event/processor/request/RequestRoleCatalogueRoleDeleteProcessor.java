package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;

/**
 * Deletes requests with role catalogue.
 * 
 * @author svandav
 *
 */
@Component(RequestRoleCatalogueRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes requests with role catalogue.")
public class RequestRoleCatalogueRoleDeleteProcessor
		extends AbstractRequestableDeleteProcessor<IdmRoleCatalogueRoleDto> {
	
	public static final String PROCESSOR_NAME = "request-role-catalogue-role-delete-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}