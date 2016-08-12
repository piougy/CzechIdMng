package eu.bcvsolutions.idm.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.AbstractUnitTest;
import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.security.service.impl.DefaultGrantedAuthoritiesFactory;

/**
 * Test for {@link DefaultGrantedAuthoritiesFactory}
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class DefaultGrantedAuthoritiesFactoryTest extends AbstractUnitTest {

	@Mock
	private IdmIdentityRepository idmIdentityRepository;
	
	@InjectMocks
	private DefaultGrantedAuthoritiesFactory defaultGrantedAuthoritiesFactory = new DefaultGrantedAuthoritiesFactory();
	
	private static final IdmRoleAuthority SUB_ROLE_AUTHORITY;
	private static final IdmIdentity TEST_IDENTITY;
	
	static {
		// prepare roles and authorities
		IdmRole subRole = new IdmRole();
		subRole.setName("sub_role");
		IdmRoleAuthority subRoleAuthority = new IdmRoleAuthority();
		subRoleAuthority.setActionPermission(IdmBasePermission.DELETE);
		subRoleAuthority.setTargetPermission(IdmGroupPermission.USER);
		SUB_ROLE_AUTHORITY = subRoleAuthority;
		subRole.getAuthorities().add(subRoleAuthority);
		IdmRole superiorRole = new IdmRole();
		superiorRole.setName("superior_role");
		IdmRoleAuthority superiorRoleAuthority = new IdmRoleAuthority();
		superiorRoleAuthority.setActionPermission(IdmBasePermission.DELETE);
		superiorRoleAuthority.setTargetPermission(IdmGroupPermission.USER);
		superiorRole.getAuthorities().add(superiorRoleAuthority);
		superiorRole.getSubRoles().add(new IdmRoleComposition(superiorRole, subRole));
		
		// prepare identity		
		IdmIdentity identity = new IdmIdentity();
		String username = "username";
		identity.setUsername(username);
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(superiorRole);
		identity.getRoles().add(identityRole);
		
		TEST_IDENTITY = identity;
	}
	
	@Test
	public void testRoleComposition() {	
		when(idmIdentityRepository.findOneByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		
		List<DefaultGrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(SUB_ROLE_AUTHORITY.getAuthority(), grantedAuthorities.get(0).getAuthority());
		
		verify(idmIdentityRepository).findOneByUsername(TEST_IDENTITY.getUsername());
	}
	
	@Test
	public void testUniqueAuthorities() {
		when(idmIdentityRepository.findOneByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		
		List<DefaultGrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(1, grantedAuthorities.size());
		
		verify(idmIdentityRepository).findOneByUsername(TEST_IDENTITY.getUsername());
	}
}
