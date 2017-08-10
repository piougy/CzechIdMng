package eu.bcvsolutions.idm.core.security.auth.filter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * @author Jan Helbich
 */
public class AuthenticationFilterTest extends AbstractIntegrationTest {

	@Autowired private SecurityService securityService;
	@Autowired private EnabledEvaluator enabledEvaluator;
	
	@Test
	public void testRemoveDisabledFilters() {
		AuthenticationFilter filter = new AuthenticationFilter(Lists.newArrayList(new TestAuthFilter()),
																	  securityService, enabledEvaluator);
		Assert.assertEquals(0, filter.getFilters().size());
		//
		filter = new AuthenticationFilter(Lists.newArrayList(new TestAuthFilter(), new JwtIdmAuthenticationFilter()),
												 securityService, enabledEvaluator);
		Assert.assertEquals(1, filter.getFilters().size());
	}

	@Enabled("randomModuleName")
	public static class TestAuthFilter extends JwtIdmAuthenticationFilter {
	}
}