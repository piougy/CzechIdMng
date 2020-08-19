package eu.bcvsolutions.idm.test.api;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;

/**
 * Authorization policy tests => disable default role to test concrete policy only.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
public class AbstractEvaluatorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private RoleConfiguration roleConfiguration;
	//
	private String defaultRoleCode = RoleConfiguration.DEFAULT_DEFAULT_ROLE;
	
	@Before
	public void disableDefaultRole() {
		defaultRoleCode = roleConfiguration.getDefaultRoleCode();
		// empty property => disable default role
		getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, "");
	}

	@After
	public void enableDefaultRole() {
		getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, defaultRoleCode);
	}
}
