package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test - find assigned role by role catalogue code.
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
@Transactional
public class IdentityRoleByRoleCatalogueCodeFilterIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Test
	public void testNonExistingCode() {
		IdmRoleDto role = getHelper().createRole();
		IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();

		getHelper().createRoleCatalogueRole(role, roleCatalogue);

		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.set(IdentityRoleByRoleCatalogueCodeFilter.PARAMETER_ROLE_CATALOGUE_CODE, "wrong" + UUID.randomUUID());
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter(params);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();

		Assert.assertTrue(identityRoles.isEmpty());
	}

	@Test
	public void testOneResult() {
		IdmRoleDto role = getHelper().createRole();
		IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();

		getHelper().createRoleCatalogueRole(role, roleCatalogue);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityRoleDto createIdentityRole = getHelper().createIdentityRole(identity, role);

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.set(IdentityRoleByRoleCatalogueCodeFilter.PARAMETER_ROLE_CATALOGUE_CODE, roleCatalogue.getCode());
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter(params);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();

		Assert.assertFalse(identityRoles.isEmpty());
		Assert.assertEquals(1, identityRoles.size());

		IdmIdentityRoleDto foundedIdentityRole = identityRoles.get(0);
		Assert.assertEquals(createIdentityRole.getId(), foundedIdentityRole.getId());
	}

	@Test
	public void testMoreResult() {
		IdmRoleDto role = getHelper().createRole();
		IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();

		getHelper().createRoleCatalogueRole(role, roleCatalogue);

		List<IdmIdentityRoleDto> roles = new ArrayList<IdmIdentityRoleDto>();
		for (int index = 0; index < 3; index++) {
			IdmIdentityDto identity = getHelper().createIdentity();
			roles.add(getHelper().createIdentityRole(identity, role));
		}

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.set(IdentityRoleByRoleCatalogueCodeFilter.PARAMETER_ROLE_CATALOGUE_CODE, roleCatalogue.getCode());
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter(params);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();

		Assert.assertFalse(identityRoles.isEmpty());
		Assert.assertEquals(3, identityRoles.size());

		for (IdmIdentityRoleDto idenitityRole : identityRoles) {
			Assert.assertTrue(roles.contains(idenitityRole));
			roles.remove(idenitityRole);
		}

		Assert.assertTrue(roles.isEmpty());
	}
}
