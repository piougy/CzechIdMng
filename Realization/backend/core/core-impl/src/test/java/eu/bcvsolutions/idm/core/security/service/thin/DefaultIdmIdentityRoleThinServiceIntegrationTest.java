package eu.bcvsolutions.idm.core.security.service.thin;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.thin.IdmIdentityRoleThinDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.thin.IdmIdentityRoleThin_;
import eu.bcvsolutions.idm.core.model.service.thin.DefaultIdmIdentityRoleThinService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Load thin identity role
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmIdentityRoleThinServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	//
	private DefaultIdmIdentityRoleThinService thinService;
	
	@Before
	public void init() {
		thinService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityRoleThinService.class);
	}
	
	@Test
	public void testGetThinEntity() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		//
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identity, role); // other
		//
		Assert.assertNull(thinService.get(UUID.randomUUID()));
		IdmIdentityRoleThinDto identityRoleThin = thinService.get(identityRole.getId());
		Assert.assertNotNull(identityRoleThin);
		Assert.assertEquals(identityRole.getId(), identityRoleThin.getId());
		Assert.assertEquals(identityRole.getRole(), identityRoleThin.getRole());
		//
		IdmRoleDto embeddedRole = DtoUtils.getEmbedded(identityRoleThin, IdmIdentityRoleThin_.role);
		Assert.assertEquals(embeddedRole.getCode(), role.getCode());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testCheckAccess() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		//
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(identity, role);
		//
		thinService.get(identityRole.getId(), IdmBasePermission.READ);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testFind() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		//
		thinService.find(filter, null);
	}
}
