package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;

/**
 * Deletes requests with policy.
 * 
 * @author svandav
 *
 */
@Component(RequestAuthorizationPolicyDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes requests with policy.")
public class RequestAuthorizationPolicyDeleteProcessor
		extends AbstractRequestableDeleteProcessor<IdmAuthorizationPolicyDto> {
	
	public static final String PROCESSOR_NAME = "request-policy-delete-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}