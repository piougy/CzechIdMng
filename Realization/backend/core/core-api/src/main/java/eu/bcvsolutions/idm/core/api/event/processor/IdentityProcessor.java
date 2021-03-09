package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Identity processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdentityProcessor extends EntityEventProcessor<IdmIdentityDto> {

	/**
	 * Skip password validation.
	 * 
	 * @since 10.5.0
	 */
	String SKIP_PASSWORD_VALIDATION = "skipPasswordValidation";
}
