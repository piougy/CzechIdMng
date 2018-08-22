package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Tests authorities change for role removal.
 * - tokens related to identity with role removal should be disabled.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 */
@Transactional
public class IdentityRoleDeleteAuthoritiesProcessorTest extends AbstractIdentityAuthoritiesProcessorTest {
	
	/**
	 * Removing a role which grants authorities must raise
	 * the authorities modification event on identity.
	 */
	@Test
	public void testRoleRemovedAuthorityRemoved() {
		IdmRoleDto role = getTestRole();
		IdmIdentityDto i = getHelper().createIdentity();
		IdmIdentityContractDto c = getTestContract(i);
		IdmIdentityRoleDto ir = getTestIdentityRole(role, c);
		
		List<IdmTokenDto> tokens = tokenManager.getTokens(i);
		//
		Assert.assertTrue(tokens.isEmpty());
		Assert.assertEquals(1, identityRoleService.findAllByIdentity(i.getId()).size());
		checkAssignedAuthorities(i);
		//
		// login - one token
		getHelper().login(i.getUsername(), i.getPassword());
		try {
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
			//
			// remove role - token should be disabled
			identityRoleService.delete(ir);
			//
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertTrue(tokens.get(0).isDisabled());
			Assert.assertEquals(0, identityRoleService.findAllByIdentity(i.getId()).size());
			Assert.assertEquals(0, authoritiesFactory.getGrantedAuthoritiesForIdentity(i.getId()).size());
		} finally {
			getHelper().logout();
		}
	}

	/**
	 * User has to roles with same authorities - removing just one role
	 * shall not change the authorities modification flag.
	 */
	@Test
	public void testRoleRemovedAuthorityStays() {
		// two roles with same authorities
		IdmRoleDto role = getTestRole();
		IdmRoleDto role2 = getTestRole();
		IdmIdentityDto i = getHelper().createIdentity();
		IdmIdentityContractDto c = getTestContract(i);
		IdmIdentityRoleDto ir = getTestIdentityRole(role, c);
		IdmIdentityRoleDto ir2 = getTestIdentityRole(role2, c);
		//
		List<IdmTokenDto> tokens = tokenManager.getTokens(i);
		//
		Assert.assertTrue(tokens.isEmpty());
		Assert.assertEquals(2, identityRoleService.findAllByIdentity(i.getId()).size());
		checkAssignedAuthorities(i);
		//
		// login - one token
		getHelper().login(i.getUsername(), i.getPassword());
		try {
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
	
			identityRoleService.delete(ir2);
			
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
			Assert.assertEquals(1, identityRoleService.findAllByIdentity(i.getId()).size());
			Assert.assertEquals(ir.getId(), identityRoleService.findAllByIdentity(i.getId()).get(0).getId());
			Assert.assertEquals(1, authoritiesFactory.getGrantedAuthoritiesForIdentity(i.getId()).size());
		} finally {
			getHelper().logout();
		}
	}

	/**
	 * User has to roles with same authorities - removing just one role
	 * shall not change the authorities modification flag.
	 */
	@Test
	public void testRoleRemovedSuperAuthorityStays() {
		// role with APP_ADMIN authority
		IdmRoleDto r = getHelper().createRole();
		createTestPolicy(r, IdmBasePermission.ADMIN, IdmGroupPermission.APP);
		//
		IdmRoleDto role2 = getTestRole();
		IdmIdentityDto i = getHelper().createIdentity();
		IdmIdentityContractDto c = getTestContract(i);
		IdmIdentityRoleDto ir = getTestIdentityRole(r, c);
		IdmIdentityRoleDto ir2 = getTestIdentityRole(role2, c);
		//
		List<IdmTokenDto> tokens = tokenManager.getTokens(i);
		//
		Assert.assertTrue(tokens.isEmpty());		
		Assert.assertEquals(2, identityRoleService.findAllByIdentity(i.getId()).size());
		//
		// login - one token
		getHelper().login(i.getUsername(), i.getPassword());
		try {
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
		
			identityRoleService.delete(ir2);
	
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
			Assert.assertEquals(1, identityRoleService.findAllByIdentity(i.getId()).size());
			Assert.assertEquals(ir.getId(), identityRoleService.findAllByIdentity(i.getId()).get(0).getId());
			Assert.assertEquals(1, authoritiesFactory.getGrantedAuthoritiesForIdentity(i.getId()).size());
		} finally {
			getHelper().logout();
		}
	}
	
	private void checkAssignedAuthorities(IdmIdentityDto i) {
		GrantedAuthority g = new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.DELETE);
		Collection<GrantedAuthority> authorities = authoritiesFactory.getGrantedAuthoritiesForIdentity(i.getId());
		Assert.assertEquals(1, authorities.size());
		authorities.stream().forEach(a -> Assert.assertEquals(g, a));
	}
	
}
