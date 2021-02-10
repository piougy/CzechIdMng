package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.filter.AuthorizationPolicyByIdentityFilterBuilder;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;

/**
 * Controller tests.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAuthorizationPolicyControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmAuthorizationPolicyDto> {

	@Autowired private IdmAuthorizationPolicyController controller;
	@Autowired private RoleConfiguration roleConfiguration;
	@Autowired private AuthorizationPolicyByIdentityFilterBuilder authorizationPolicyByIdentityFilterBuilder;
	
	@Override
	protected AbstractReadWriteDtoController<IdmAuthorizationPolicyDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}

	@Override
	protected IdmAuthorizationPolicyDto prepareDto() {
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setGroupPermission(IdmGroupPermission.APP.getName());
		dto.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		dto.setRole(getHelper().createRole().getId());
		dto.setEvaluator(BasePermissionEvaluator.class);
		return dto;
	}
	
	@Test
	public void testFindByIdentityId() {
		// default role is enabled by default - disable
		String defaultRoleCode = roleConfiguration.getDefaultRoleCode();
		//
		try {
			//disable default role
			getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, "");
			//
			// create test data
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			//
			IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
			filter.setIdentityId(identity.getId());
			List<IdmAuthorizationPolicyDto> policies = find(filter);
			Assert.assertTrue(policies.isEmpty());
			//
			// assign role
			IdmRoleDto role = getHelper().createRole();
			IdmAuthorizationPolicyDto policy = getHelper().createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE);
			getHelper().createIdentityRole(identity, role);
			//
			policies = find(filter);
			Assert.assertEquals(1, policies.size());
			Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(policy.getId())));
			//
			// configure default role
			IdmRoleDto defaultRole = getHelper().createRole();
			IdmAuthorizationPolicyDto defaultPolicy = getHelper().createBasePolicy(defaultRole.getId(), IdmBasePermission.READ);
			getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, defaultRole.getId().toString());
			//
			policies = find(filter);
			Assert.assertEquals(2, policies.size());
			Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(policy.getId())));
			Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(defaultPolicy.getId())));
		} finally {
			getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, defaultRoleCode);
		}
	}
	
	@Test
	public void testFindByRoleId() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleOther = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto policyOne = getHelper().createBasePolicy(roleOne.getId(), IdmBasePermission.READ);
		getHelper().createBasePolicy(roleOther.getId(), IdmBasePermission.UPDATE); // other
		//
		//
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(roleOne.getId());
		List<IdmAuthorizationPolicyDto> policies = find(filter);
		Assert.assertEquals(1, policies.size());
		Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
	}
	
	@Test
	public void testFindByGroupPermission() {
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto policyOne = getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.READ);
		// other
		getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.ROLE,
				IdmRole.class,
				IdmBasePermission.READ);
		//
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(role.getId());
		filter.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		List<IdmAuthorizationPolicyDto> policies = find(filter);
		Assert.assertEquals(1, policies.size());
		Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
	}
	
	@Test
	public void testFindByAuthorizableType() {
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto policyOne = getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.READ);
		// other
		getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.ROLE,
				IdmRole.class,
				IdmBasePermission.READ);
		//
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(role.getId());
		filter.setAuthorizableType(IdmIdentity.class.getCanonicalName()); // FIXME: move to api
		List<IdmAuthorizationPolicyDto> policies = find(filter);
		Assert.assertEquals(1, policies.size());
		Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
	}
	
	@Test
	public void testFindByText() {
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto policyOne = getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.READ);
		// other
		getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.ROLE,
				IdmRole.class,
				IdmBasePermission.READ);
		//
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(role.getId());
		filter.setText(IdmIdentity.class.getSimpleName());
		List<IdmAuthorizationPolicyDto> policies = find(filter);
		Assert.assertEquals(1, policies.size());
		Assert.assertTrue(policies.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
	}
	
	@Test 
	public void testEmptyPredicates() {
		Assert.assertNull(authorizationPolicyByIdentityFilterBuilder.getPredicate(null, null, null, new IdmAuthorizationPolicyFilter()));
	}
	
	@Test
	public void testCreateLongPermissions() {
		IdmAuthorizationPolicyDto prepareDto = prepareDto();
		prepareDto.setBasePermissions(StringUtils.repeat('x', 2000));
		//
		IdmAuthorizationPolicyDto createDto = createDto(prepareDto);
		//
		Assert.assertEquals(prepareDto.getPermissions(), createDto.getPermissions());
		
		
	}
}
