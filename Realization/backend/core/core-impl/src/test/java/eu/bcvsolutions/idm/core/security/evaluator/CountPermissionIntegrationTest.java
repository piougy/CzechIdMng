package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test count base permission
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class CountPermissionIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmIdentityService service;
	
	@Test
	public void testCountPermission() {
		IdmIdentityDto identitySelf = getHelper().createIdentity();
		IdmIdentityDto identityOther = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identitySelf, role);
		//
		// add count permission to other
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(CodeableEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		dto.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		dto.getEvaluatorProperties().put(CodeableEvaluator.PARAMETER_IDENTIFIER, identityOther.getId());
		dto.setPermissions(IdmBasePermission.COUNT);
		authorizationPolicyService.save(dto);
		//
		// add read permission to self
		dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(CodeableEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		dto.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		dto.getEvaluatorProperties().put(CodeableEvaluator.PARAMETER_IDENTIFIER, identitySelf.getId());
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		
		try {
			getHelper().login(identitySelf.getUsername(), TestHelper.DEFAULT_PASSWORD);
			//
			// check - count - other
			Assert.assertEquals(1, service.count(null, IdmBasePermission.COUNT));
			List<IdmIdentityDto> results = service.find(null, IdmBasePermission.COUNT).getContent();
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(identityOther.getId(), results.get(0).getId());
			//
			// check - read - self
			results = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(identitySelf.getId(), results.get(0).getId());
		} finally {
			logout();
		}
	}
}
