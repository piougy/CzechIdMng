package eu.bcvsolutions.idm.core.eav.service.impl;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Default identity default configured as identity form projection:
 * - localization a "big" button can be shown
 * - authorization policies can be defined
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(DefaultFormProjectionRoute.PROJECTION_NAME)
public class DefaultFormProjectionRoute extends AbstractFormProjectionRoute<IdmIdentity> {
	
	public static final String PROJECTION_NAME = "/form/identity";
	
	@Override
	public String getName() {
		return PROJECTION_NAME;
	}
}
