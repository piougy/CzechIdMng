package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;

/**
 * Deletes requests with role guarantee role.
 * 
 * @author svandav
 *
 */
@Component(RequestRoleGuaranteeRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes requests with role guarantee role.")
public class RequestRoleGuaranteeRoleDeleteProcessor
		extends AbstractRequestableDeleteProcessor<IdmRoleGuaranteeRoleDto> {
	
	public static final String PROCESSOR_NAME = "request-role-guarantee-role-delete-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}