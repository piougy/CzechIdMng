package eu.bcvsolutions.idm.example.eav.service.impl;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Example identity form projection - combined different projections for create and edit identity:
 * - create by projection
 * - edit by default product form
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component(ExampleCombinedIdentityFormProjectionRoute.PROJECTION_NAME)
public class ExampleCombinedIdentityFormProjectionRoute extends AbstractFormProjectionRoute<IdmIdentity> {
	
	public static final String PROJECTION_NAME = "/example/form/combined-identity-projection";
	
	@Override
	public String getName() {
		return PROJECTION_NAME;
	}
}
