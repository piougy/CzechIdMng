package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Authorization policy evaluator test.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class IdentityByFormProjectionEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private LookupService lookupService;
	@Autowired private IdmFormProjectionService projectionService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Test
	public void testPolicy() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto identityOther = getHelper().createIdentity();
		IdmFormProjectionDto projection = new IdmFormProjectionDto();
		projection.setCode(getHelper().createName());
		projection.setOwnerType(lookupService.getOwnerType(IdmIdentityDto.class));
		projection = projectionService.save(projection);
		identity.setFormProjection(projection.getId());
		IdmIdentityDto identityOne = identityService.save(identity);
				
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);
		//
		List<IdmIdentityDto> identities = null;
		//
		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			identities = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(identities.isEmpty());	
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(IdentityByFormProjectionEvaluator.PARAMETER_FORM_PROJECTION, projection.getId());
		IdmAuthorizationPolicyDto authorizationPolicy = getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdentityByFormProjectionEvaluator.class,
				properties,
				IdmBasePermission.READ);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			identities = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, identities.size());	
			Assert.assertEquals(identityOne.getId(), identities.get(0).getId());
			//
			Set<String> permissions = identityService.getPermissions(identityOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
		//
		// default user type
		authorizationPolicyService.delete(authorizationPolicy);
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdentityByFormProjectionEvaluator.class,
				IdmBasePermission.READ);
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			identities = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertFalse(identities.isEmpty());	
			Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityOther.getId())));
			Assert.assertTrue(identities.stream().allMatch(i -> !i.getId().equals(identityOne.getId())));
			//
			Set<String> permissions = identityService.getPermissions(identityOther);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
	}
}
