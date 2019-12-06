package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;

/**
 * Deletes requests with role guarantee.
 * 
 * @author svandav
 *
 */
@Component(RequestRoleGuaranteeDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes requests with role guarantee.")
public class RequestRoleGuaranteeDeleteProcessor
		extends AbstractRequestableDeleteProcessor<IdmRoleGuaranteeDto> {
	
	public static final String PROCESSOR_NAME = "request-role-guarantee-delete-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}