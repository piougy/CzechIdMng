package eu.bcvsolutions.idm.example.eav.service.impl;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Example identity form projection.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(ExampleIdentityFormProjectionRoute.PROJECTION_NAME)
public class ExampleIdentityFormProjectionRoute extends AbstractFormProjectionRoute<IdmIdentity> {
	
	public static final String PROJECTION_NAME = "/example/form/identity-projection";
	
	@Override
	public String getName() {
		return PROJECTION_NAME;
	}
}
