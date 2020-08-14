package eu.bcvsolutions.idm.core.delegation;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.delegation.type.DefaultDelegationType;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefinitionByDelegatorEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Delegation definition by delegator evaluator test.
 *
 * @author Vít Švanda
 */
@Transactional
public class DelegationDefinitionByDelegatorEvaluatorTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmDelegationDefinitionService delegationDefinitionService;

	@Test
	public void testRead() {

		IdmIdentityDto delegatorOne = getHelper().createIdentity();
		IdmIdentityDto delegatorTwo = getHelper().createIdentity();
		IdmIdentityContractDto primeContactDelegatorOne = getHelper().getPrimeContract(delegatorOne);

		IdmIdentityDto delegateOne = getHelper().createIdentity();
		IdmIdentityDto delegateTwo = getHelper().createIdentity();

		// Create default delegation One.
		IdmDelegationDefinitionDto definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegatorOne.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne = delegationDefinitionService.save(definitionOne);

		// Create default delegation Two.
		IdmDelegationDefinitionDto definitionTwo = new IdmDelegationDefinitionDto();
		definitionTwo.setType(DefaultDelegationType.NAME);
		definitionTwo.setDelegator(delegatorTwo.getId());
		definitionTwo.setDelegate(delegateTwo.getId());
		delegationDefinitionService.save(definitionTwo);
		//
		List<IdmIdentityDto> identities;
		List<IdmIdentityContractDto> contracts;
		List<IdmDelegationDefinitionDto> delegationDefinitions;
		IdmRoleDto roleWithPermissions = getHelper().createRole();
		//
		getHelper().createIdentityRole(delegatorOne, roleWithPermissions);
		getHelper().createIdentityRole(delegatorTwo, roleWithPermissions);
		//
		// check - read without policy
		try {
			getHelper().login(delegatorOne);
			//
			identities = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(identities.isEmpty());
			contracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(contracts.isEmpty());
			delegationDefinitions = delegationDefinitionService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(delegationDefinitions.isEmpty());
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		// identity
		getHelper().createAuthorizationPolicy(
				roleWithPermissions.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SelfIdentityEvaluator.class, IdmBasePermission.READ);
		// delegation transitively
		getHelper().createAuthorizationPolicy(
				roleWithPermissions.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				DelegationDefinitionByDelegatorEvaluator.class);
		//
		try {
			getHelper().login(delegatorOne);
			//
			// without update permission
			identities = identityService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(identities.isEmpty());
			contracts = contractService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(contracts.isEmpty());
			delegationDefinitions = delegationDefinitionService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(delegationDefinitions.isEmpty());
			//
			// evaluate	access
			identities = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, identities.size());
			Assert.assertEquals(delegatorOne.getId(), identities.get(0).getId());
			contracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(0, contracts.size());
			delegationDefinitions = delegationDefinitionService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, delegationDefinitions.size());
			Assert.assertEquals(definitionOne.getId(), delegationDefinitions.get(0).getId());
			//
			Set<String> permissions = identityService.getPermissions(delegatorOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			permissions = contractService.getPermissions(primeContactDelegatorOne);
			Assert.assertEquals(0, permissions.size());
			permissions = delegationDefinitionService.getPermissions(definitionOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		} finally {
			logout();
		}
	}
}
