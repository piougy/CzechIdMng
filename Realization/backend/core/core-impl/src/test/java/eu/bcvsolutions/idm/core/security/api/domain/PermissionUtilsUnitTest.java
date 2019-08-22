package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * {@link PermissionUtils} static utility test
 * 
 * @author Radek Tomiška
 *
 */
public class PermissionUtilsUnitTest extends AbstractUnitTest {

	@Test
	public void testTrimNullWithNotNullElements() {
		BasePermission[] input = new IdmBasePermission[]{ IdmBasePermission.READ, IdmBasePermission.UPDATE };
		BasePermission[] result = PermissionUtils.trimNull(input);
		//
		Assert.assertArrayEquals(input, result);
	}
	
	@Test
	public void testTrimNullWithNullElements() {
		BasePermission[] input = new IdmBasePermission[]{ IdmBasePermission.READ, null, IdmBasePermission.UPDATE, null };
		BasePermission[] result = PermissionUtils.trimNull(input);
		//
		Assert.assertArrayEquals(new IdmBasePermission[]{ IdmBasePermission.READ, IdmBasePermission.UPDATE }, result);
	}
	
	@Test
	public void testTrimNullWithNull() {
		BasePermission[] input = null;
		BasePermission[] result = PermissionUtils.trimNull(input);
		//
		Assert.assertArrayEquals(input, result);
	}
	
	@Test
	public void testHasPermissionSimple() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		Assert.assertTrue(PermissionUtils.hasPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ));
	}
	
	@Test
	public void testHasPermissionAdmin() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.ADMIN);
		//
		Assert.assertTrue(PermissionUtils.hasPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ));
	}
	
	@Test
	public void testHasPermissionAdminMulti() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.ADMIN);
		//
		Assert.assertTrue(PermissionUtils.hasPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ, IdmBasePermission.DELETE));
	}
	
	@Test
	public void testHasNotPermission() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		Assert.assertFalse(PermissionUtils.hasPermission(PermissionUtils.toString(permissions), IdmBasePermission.DELETE));
	}
	
	@Test
	public void testHasPermissionMulti() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		Assert.assertTrue(PermissionUtils.hasPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ, IdmBasePermission.UPDATE));
	}
	
	@Test
	public void testHasNotPermissionMulti() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		Assert.assertFalse(PermissionUtils.hasPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ, IdmBasePermission.DELETE));
	}
	
	@Test
	public void testHasAnyPermissionSingle() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		Assert.assertTrue(PermissionUtils.hasAnyPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ));
	}
	
	@Test
	public void testHasAnyPermissionMulti() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		Assert.assertTrue(PermissionUtils.hasAnyPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ, IdmBasePermission.DELETE));
	}
	
	@Test
	public void testHasAnyPermissionAdmin() {
		Set<BasePermission> permissions = Sets.newHashSet(IdmBasePermission.ADMIN);
		//
		Assert.assertTrue(PermissionUtils.hasAnyPermission(PermissionUtils.toString(permissions), IdmBasePermission.READ, IdmBasePermission.DELETE));
	}
	
	@Test
	public void testToPermissions() {
		Assert.assertTrue(PermissionUtils.toPermissions(null).isEmpty());
		Assert.assertTrue(PermissionUtils.toPermissions(Sets.newHashSet()).isEmpty());
		//
		Set<String> pelMel = Sets.newHashSet(
				CoreGroupPermission.AUTHORIZATIONPOLICY_AUTOCOMPLETE, 
				CoreGroupPermission.IDENTITY_AUTOCOMPLETE, 
				IdmBasePermission.CREATE.getName(),
				"IDENTITY_CUSTOM",
				RoleBasePermission.CANBEREQUESTED.getName(),
				CoreGroupPermission.ROLE_CANBEREQUESTED);
		//
		Collection<BasePermission> permissions = PermissionUtils.toPermissions(pelMel);
		//
		Assert.assertEquals(4, permissions.size());
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.getName().equals(IdmBasePermission.CREATE.getName())));
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.getName().equals(IdmBasePermission.AUTOCOMPLETE.getName())));
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.getName().equals(RoleBasePermission.CANBEREQUESTED.getName())));
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.getName().equals("CUSTOM")));
	}
}
