package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;

/**
 * Deletes requests with role composition.
 * 
 * @author svandav
 *
 */
@Component(RequestRoleCompositionDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes requests with role composition.")
public class RequestRoleCompositionDeleteProcessor
		extends AbstractRequestableDeleteProcessor<IdmRoleCompositionDto> {
	
	public static final String PROCESSOR_NAME = "request-role-composition-delete-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}