package eu.bcvsolutions.idm.core.model.delegation.type;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractDelegationType;
import org.springframework.stereotype.Component;

/**
 *
 * Default delegation type for all tasks.
 *
 * @author Vít Švanda
 */
@Component(DefaultDelegationType.NAME)
public class DefaultDelegationType extends AbstractDelegationType {

	public static final String NAME = "default-delegation-type";

	@Override
	public Class<? extends BaseDto> getOwnerType() {
		// Default delegation is for all types.
		return null;
	}

	@Override
	public boolean isSupportsDelegatorContract() {
		return false;
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
