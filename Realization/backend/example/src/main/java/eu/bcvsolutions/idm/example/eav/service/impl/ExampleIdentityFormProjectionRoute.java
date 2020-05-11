package eu.bcvsolutions.idm.example.eav.service.impl;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Example identity form projection.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component(ExampleIdentityFormProjectionRoute.PROJECTION_NAME)
public class ExampleIdentityFormProjectionRoute extends AbstractFormProjectionRoute<IdmIdentity> {
	
	public static final String PROJECTION_NAME = "/example/form/identity-projection";
	
	@Override
	public String getName() {
		return PROJECTION_NAME;
	}
}
