package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;

/**
 * Deletes requests with role.
 * 
 * @author svandav
 *
 */
@Component(RequestRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes requests with role.")
public class RequestRoleDeleteProcessor
		extends AbstractRequestableDeleteProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "request-role-delete-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}