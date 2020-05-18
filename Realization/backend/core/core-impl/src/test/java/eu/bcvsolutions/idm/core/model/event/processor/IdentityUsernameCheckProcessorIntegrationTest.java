package eu.bcvsolutions.idm.core.model.event.processor;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests for check username duplicities.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Transactional
public class IdentityUsernameCheckProcessorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityService identityService;

	@Test
	public void testUniqueUsername() {
		String testUsername = getHelper().createName();

		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(testUsername);
		
		identity = identityService.save(identity);
		
		IdmIdentityDto byUsername = identityService.getByUsername(testUsername);
		Assert.assertNotNull(byUsername);
		Assert.assertEquals(testUsername, byUsername.getUsername());
		identityService.delete(byUsername);
		Assert.assertNull(identityService.getByUsername(testUsername));
	}

	@Test(expected = ResultCodeException.class)
	public void testDuplcityUsername() {
		String testUsername = getHelper().createName();

		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(testUsername);
		
		identity = identityService.save(identity);
		
		IdmIdentityDto byUsername = identityService.getByUsername(testUsername);
		Assert.assertNotNull(byUsername);
		Assert.assertEquals(testUsername, byUsername.getUsername());

		IdmIdentityDto identitySecond = new IdmIdentityDto();
		identitySecond.setUsername(testUsername);

		identityService.save(identitySecond);
	}
}
