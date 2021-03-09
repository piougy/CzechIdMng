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
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete password policies integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class PasswordPolicyDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmPasswordPolicyService service;
	
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
		List<IdmPasswordPolicyDto> policies = createPolicies(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmPasswordPolicy.class, PasswordPolicyDeleteBulkAction.NAME);
		
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
		List<IdmPasswordPolicyDto> policies = createPolicies(5);
		
		IdmPasswordPolicyFilter filter = new IdmPasswordPolicyFilter();
		filter.setId(policies.get(2).getId());

		List<IdmPasswordPolicyDto> checkPolicies = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkPolicies.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmPasswordPolicy.class, PasswordPolicyDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(policies.get(2)));
		Assert.assertNotNull(service.get(policies.get(1)));
		Assert.assertNotNull(service.get(policies.get(3)));
	}
	
	private List<IdmPasswordPolicyDto> createPolicies(int count) {
		List<IdmPasswordPolicyDto> results = new ArrayList<>();
		//
		for (int i = 0; i < count; i++) {
			IdmPasswordPolicyDto dto = new IdmPasswordPolicyDto();
			dto.setName(getHelper().createName());
			dto.setType(IdmPasswordPolicyType.VALIDATE);
			results.add(service.save(dto));
		}
		//
		return results;
	}
}
