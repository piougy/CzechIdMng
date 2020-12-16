package eu.bcvsolutions.idm.core.bulk.action.impl.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete authorization policies integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class AuthorizationPolicyDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmAuthorizationPolicyService service;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmAuthorizationPolicyDto> policies = createPolicies(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmAuthorizationPolicy.class, AuthorizationPolicyDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(policies);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmAuthorizationPolicyDto> policies = createPolicies(5);
		
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setId(policies.get(2).getId());

		List<IdmAuthorizationPolicyDto> checkPolicies = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkPolicies.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmAuthorizationPolicy.class, AuthorizationPolicyDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(policies.get(2)));
		Assert.assertNotNull(service.get(policies.get(1)));
		Assert.assertNotNull(service.get(policies.get(3)));
	}
	
	private List<IdmAuthorizationPolicyDto> createPolicies(int count) {
		List<IdmAuthorizationPolicyDto> results = new ArrayList<>();
		//
		for (int i = 0; i < count; i++) {
			IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
			dto.setGroupPermission(IdmGroupPermission.APP.getName());
			dto.setPermissions(IdmBasePermission.AUTOCOMPLETE);
			dto.setRole(getHelper().createRole().getId());
			dto.setEvaluator(BasePermissionEvaluator.class);
			results.add(service.save(dto));
		}
		//
		return results;
	}
}
