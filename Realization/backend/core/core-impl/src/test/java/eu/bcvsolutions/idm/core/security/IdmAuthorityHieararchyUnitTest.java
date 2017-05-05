package eu.bcvsolutions.idm.core.security;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.core.security.service.impl.IdmAuthorityHierarchy;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Authorities hierarchy test
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAuthorityHieararchyUnitTest extends AbstractUnitTest {

	@Mock
	private ModuleService moduleService;
	//
	private IdmAuthorityHierarchy hierarchy;
	
	@Before
	public void init() {		
		hierarchy = new IdmAuthorityHierarchy(moduleService);
	}
	
	@Test
	public void testSimpleRole() {
		Mockito.when(moduleService.getAvailablePermissions()).thenReturn(Arrays.asList(CoreGroupPermission.values()));
		//
		Collection<?> authorities = hierarchy.getReachableGrantedAuthorities(Lists.newArrayList(new DefaultGrantedAuthority(CoreGroupPermission.AUDIT_READ)));
		Assert.assertEquals(1, authorities.size());
		Assert.assertEquals(new DefaultGrantedAuthority(CoreGroupPermission.AUDIT_READ), authorities.iterator().next());
	}
	
	@Test
	public void testSimpleRoleAsString() {
		Mockito.when(moduleService.getAvailablePermissions()).thenReturn(Arrays.asList(CoreGroupPermission.values()));
		//
		Collection<?> authorities = hierarchy.getReachableGrantedAuthorities(Lists.newArrayList(new DefaultGrantedAuthority("test")));
		Assert.assertEquals(1, authorities.size());
		Assert.assertEquals(new DefaultGrantedAuthority("test"), authorities.iterator().next());
	}
	
	@Test
	public void testSuperAdminRole() {
		Mockito.when(moduleService.getAvailablePermissions()).thenReturn(Arrays.asList(CoreGroupPermission.values()));
		//
		Collection<?> all = IdmAuthorityUtils.toAuthorities(moduleService.getAvailablePermissions());
		Collection<?> authorities = hierarchy.getReachableGrantedAuthorities(Lists.newArrayList(new DefaultGrantedAuthority(IdmGroupPermission.APP_ADMIN)));
		Assert.assertEquals(all.size(), authorities.size());
		Assert.assertTrue(authorities.containsAll(all));
	}
	
	@Test
	public void testGroupAdminRole() {
		Mockito.when(moduleService.getAvailablePermissions()).thenReturn(Arrays.asList(CoreGroupPermission.values()));
		//
		Collection<?> all = IdmAuthorityUtils.toAuthorities(CoreGroupPermission.ROLE);
		Collection<?> authorities = hierarchy.getReachableGrantedAuthorities(Lists.newArrayList(new DefaultGrantedAuthority(CoreGroupPermission.ROLE, IdmBasePermission.ADMIN)));
		Assert.assertEquals(all.size(), authorities.size());
		Assert.assertTrue(authorities.containsAll(all));
	}
	
	
	
}
