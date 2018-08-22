package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * IdentityGuaranteesForRoleFilter test
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class IdentityGuaranteesForRoleFilterIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdentityGuaranteesForRoleFilter filter;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testFind() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityThree = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto roleGuarantee = getHelper().createRole();
		getHelper().createRoleGuarantee(role, identityOne);
		getHelper().createRoleGuaranteeRole(role, roleGuarantee);
		getHelper().createIdentityRole(identityThree, roleGuarantee);
		getHelper().createIdentityRole(identityTwo, role);
		//
		IdmIdentityFilter dataFilter = new IdmIdentityFilter();
		dataFilter.setGuaranteesForRole(role.getId());
		List<IdmIdentity> identities = filter.find(dataFilter, null).getContent();
		//
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityOne.getId())));
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityThree.getId())));
	}
	
	@Test
	public void testFindByServiceAlias() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityThree = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto roleGuarantee = getHelper().createRole();
		getHelper().createRoleGuarantee(role, identityOne);
		getHelper().createRoleGuaranteeRole(role, roleGuarantee);
		getHelper().createIdentityRole(identityThree, roleGuarantee);
		getHelper().createIdentityRole(identityTwo, role);
		//
		List<IdmIdentityDto> identities = identityService.findGuaranteesByRoleId(role.getId(), null).getContent();
		//
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityOne.getId())));
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityThree.getId())));
		//
		identities = identityService.findGuaranteesByRoleId(role.getId(), null).getContent();
		//
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityOne.getId())));
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityThree.getId())));
	}
	
	
}
