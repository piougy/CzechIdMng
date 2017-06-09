package eu.bcvsolutions.idm.core.model.event.processor;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Tests authority change for role addition.
 * 
 * @author Jan Helbich
 *
 */
public class IdentityRoleAddAuthoritiesProcessorTest extends AbstractIdentityAuthoritiesProcessorTest {
	
	@Test
	public void testAddRoleModifyAuthorities() {
		IdmRole role = getTestRole();
		IdmIdentityDto i = getTestUser();
		IdmIdentityContractDto c = getTestContract(i);
		IdmAuthorityChange ac = getAuthorityChange(i);
		
		// user has no authorities - change flag is null
		Assert.assertNull(ac);

		// authority added
		getTestIdentityRole(role, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
	}
	
	@Test
	public void testAddRoleDoNotModifyAuthorities() throws Exception {
		IdmRole role = getTestRole();
		IdmRole role2 = getTestRole();
		IdmIdentityDto i = getTestUser();
		IdmIdentityContractDto c = getTestContract(i);
		IdmAuthorityChange ac = getAuthorityChange(i);
		
		// user has no authorities - change flag is null
		Assert.assertNull(ac);

		// authority added
		getTestIdentityRole(role, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		
		DateTime firstChangeTs = ac.getAuthChangeTimestamp();
		Assert.assertNotNull(firstChangeTs);
		
		Thread.sleep(10);
		
		getTestIdentityRole(role2, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		Assert.assertEquals(firstChangeTs, ac.getAuthChangeTimestamp());
	}

}
