package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Tests authority change for role addition.
 *
 * @author Jan Helbich
 */
public class IdentityRoleAddAuthoritiesProcessorTest extends AbstractIdentityAuthoritiesProcessorTest {

	@Test
	public void testAddRoleModifyAuthorities() {
		IdmRoleDto role = getTestRole();
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
		IdmRoleDto role = getTestRole();
		IdmRoleDto role2 = getTestRole();
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

	@Test
	public void testAddRoleWithoutAuthorities() throws Exception {
		IdmRoleDto role = getTestRole();
		IdmIdentityDto i = getTestUser();
		IdmIdentityContractDto c = getTestContract(i);
		IdmAuthorityChange ac = getAuthorityChange(i);
		// user has no authorities - change flag is null
		Assert.assertNull(ac);
		// authority added
		getTestIdentityRole(role, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		//
		DateTime firstChangeTs = ac.getAuthChangeTimestamp();
		Assert.assertNotNull(firstChangeTs);
		// prepare role without authorities
		IdmRoleDto r = new IdmRoleDto();
		r.setName(UUID.randomUUID().toString());
		r = saveInTransaction(r, roleService);

		Thread.sleep(10);

		getTestIdentityRole(r, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		Assert.assertEquals(firstChangeTs, ac.getAuthChangeTimestamp());
	}

	@Test
	public void testAddRoleWithSuperAuthorities() throws Exception {
		// prepare role with full authorities (APP_ADMIN)
		IdmRoleDto r = new IdmRoleDto();
		r.setName(UUID.randomUUID().toString());
		r = saveInTransaction(r, roleService);
		getTestPolicy(r, IdmBasePermission.ADMIN, IdmGroupPermission.APP);
		//
		IdmIdentityDto i = getTestUser();
		IdmIdentityContractDto c = getTestContract(i);
		IdmAuthorityChange ac = getAuthorityChange(i);
		Assert.assertNull(ac);
		// authority added
		getTestIdentityRole(r, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		//
		DateTime firstChangeTs = ac.getAuthChangeTimestamp();
		Assert.assertNotNull(firstChangeTs);

		// role adds IDENTITY_DELETE - must pass without auth change
		IdmRoleDto role = getTestRole();

		Thread.sleep(10);

		getTestIdentityRole(role, c);
		i = identityService.get(i.getId());
		ac = getAuthorityChange(i);
		Assert.assertEquals(firstChangeTs, ac.getAuthChangeTimestamp());
	}

}
