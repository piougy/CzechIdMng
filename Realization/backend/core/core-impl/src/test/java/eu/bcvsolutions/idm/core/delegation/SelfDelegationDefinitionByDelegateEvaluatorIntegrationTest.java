package eu.bcvsolutions.idm.core.delegation;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.SelfDelegationDefinitionByDelegateEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permission to delegation definition.
 *
 * @author Vít Švanda
 */
@Transactional
public class SelfDelegationDefinitionByDelegateEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private IdmDelegationDefinitionService service;

	@Test
	public void testPolicy() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);

		List<IdmDelegationDefinitionDto> delegations = null;
		IdmDelegationDefinitionDto delegation = getHelper().createDelegation(identity, delegator);
		;
		getHelper().createDelegation(delegate, delegator); // other

		try {
			getHelper().login(identity);
			delegations = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(delegations.isEmpty());
		} finally {
			logout();
		}

		// Create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				SelfDelegationDefinitionByDelegateEvaluator.class,
				IdmBasePermission.READ);

		try {
			getHelper().login(identity);

			// evaluate	access
			delegations = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, delegations.size());
			Assert.assertEquals(delegation.getId(), delegations.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(delegation);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
	}
}
