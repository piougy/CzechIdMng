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
 * Enable authorization policies integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class AuthorizationPolicyEnableBulkActionIntegrationTest extends AbstractBulkActionTest {

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
		IdmBulkActionDto bulkAction = findBulkAction(IdmAuthorizationPolicy.class, AuthorizationPolicyEnableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(policies);
		for (UUID id : ids) {
			Assert.assertTrue(service.get(id).isDisabled());
		}
		//
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		//
		for (UUID id : ids) {
			Assert.assertFalse(service.get(id).isDisabled());
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmAuthorizationPolicyDto> policies = createPolicies(5);
		
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setId(policies.get(2).getId());

		List<IdmAuthorizationPolicyDto> checkPolicies = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkPolicies.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmAuthorizationPolicy.class, AuthorizationPolicyEnableBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertFalse(service.get(policies.get(2)).isDisabled());
		Assert.assertTrue(service.get(policies.get(1)).isDisabled());
		Assert.assertTrue(service.get(policies.get(3)).isDisabled());
	}
	
	private List<IdmAuthorizationPolicyDto> createPolicies(int count) {
		List<IdmAuthorizationPolicyDto> results = new ArrayList<>();
		//
		for (int i = 0; i < count; i++) {
			IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
			dto.setGroupPermission(IdmGroupPermission.APP.getName());
			dto.setPermissions(IdmBasePermission.AUTOCOMPLETE);
			dto.setRole(getHelper().createRole().getId());
			dto.setDisabled(true);
			dto.setEvaluator(BasePermissionEvaluator.class);
			results.add(service.save(dto));
		}
		//
		return results;
	}
}
