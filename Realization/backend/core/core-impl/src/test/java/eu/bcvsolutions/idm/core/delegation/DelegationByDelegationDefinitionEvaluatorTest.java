package eu.bcvsolutions.idm.core.delegation;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.security.evaluator.identity.*;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.delegation.type.DefaultDelegationType;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationByDelegationDefinitionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefinitionByDelegateEvaluator;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.UUID;

/**
 * Delegation by delegation definition evaluator test.
 *
 * @author Vít Švanda
 */
@Transactional
public class DelegationByDelegationDefinitionEvaluatorTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmDelegationDefinitionService delegationDefinitionService;
	@Autowired
	private IdmDelegationService delegationService;

	@Test
	public void testRead() {

		IdmIdentityDto delegatorOne = getHelper().createIdentity();
		IdmIdentityDto delegatorTwo = getHelper().createIdentity();
		IdmIdentityContractDto primeContactDelegatorOne = getHelper().getPrimeContract(delegatorOne);

		IdmIdentityDto delegateOne = getHelper().createIdentity();
		IdmIdentityDto delegateTwo = getHelper().createIdentity();

		// Create default delegation One.
		IdmDelegationDefinitionDto delegationDefOne = new IdmDelegationDefinitionDto();
		delegationDefOne.setType(DefaultDelegationType.NAME);
		delegationDefOne.setDelegator(delegatorOne.getId());
		delegationDefOne.setDelegate(delegateOne.getId());
		delegationDefOne = delegationDefinitionService.save(delegationDefOne);

		// Create default delegation Two.
		IdmDelegationDefinitionDto delegationDefTwo = new IdmDelegationDefinitionDto();
		delegationDefTwo.setType(DefaultDelegationType.NAME);
		delegationDefTwo.setDelegator(delegatorTwo.getId());
		delegationDefTwo.setDelegate(delegateTwo.getId());
		delegationDefTwo = delegationDefinitionService.save(delegationDefTwo);
		
		IdmDelegationDto delegationOne = new IdmDelegationDto();
		delegationOne.setDefinition(delegationDefOne.getId());
		delegationOne.setOwnerId(UUID.randomUUID());
		delegationOne.setOwnerType(WorkflowTaskInstanceDto.class.getCanonicalName());
		delegationOne = delegationService.save(delegationOne);
		
		IdmDelegationDto delegationTwo = new IdmDelegationDto();
		delegationTwo.setDefinition(delegationDefTwo.getId());
		delegationTwo.setOwnerId(UUID.randomUUID());
		delegationTwo.setOwnerType(WorkflowTaskInstanceDto.class.getCanonicalName());
		delegationService.save(delegationTwo);
		
		List<IdmIdentityDto> identities;
		List<IdmIdentityContractDto> contracts;
		List<IdmDelegationDefinitionDto> delegationDefinitions;
		List<IdmDelegationDto> delegations;
		IdmRoleDto roleWithPermissions = getHelper().createRole();
		
		getHelper().createIdentityRole(delegatorOne, roleWithPermissions);
		getHelper().createIdentityRole(delegatorTwo, roleWithPermissions);
		getHelper().createIdentityRole(delegateOne, roleWithPermissions);
		getHelper().createIdentityRole(delegateTwo, roleWithPermissions);
		
		// check - read without policy
		try {
			getHelper().login(delegateOne.getUsername(), delegateOne.getPassword());
			//
			identities = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(identities.isEmpty());
			contracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(contracts.isEmpty());
			delegationDefinitions = delegationDefinitionService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(delegationDefinitions.isEmpty());
			delegations = delegationService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(delegations.isEmpty());
		} finally {
			logout();
		}
		
		// create authorization policy - assign to role
		// identity
		getHelper().createAuthorizationPolicy(
				roleWithPermissions.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SelfIdentityEvaluator.class, IdmBasePermission.READ);
		// delegation definitions transitively
		getHelper().createAuthorizationPolicy(
				roleWithPermissions.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				DelegationDefinitionByDelegateEvaluator.class);
		// delegation transitively
		getHelper().createAuthorizationPolicy(
				roleWithPermissions.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegation.class,
				DelegationByDelegationDefinitionEvaluator.class);
		//
		try {
			getHelper().login(delegateOne.getUsername(), delegateOne.getPassword());
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
			Assert.assertEquals(delegateOne.getId(), identities.get(0).getId());
			contracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(0, contracts.size());
			delegationDefinitions = delegationDefinitionService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, delegationDefinitions.size());
			Assert.assertEquals(delegationDefOne.getId(), delegationDefinitions.get(0).getId());
			
			delegations = delegationService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, delegations.size());
			Assert.assertEquals(delegationOne.getId(), delegations.get(0).getId());
			//
			Set<String> permissions = identityService.getPermissions(delegateOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			permissions = contractService.getPermissions(primeContactDelegatorOne);
			Assert.assertEquals(0, permissions.size());
			permissions = delegationDefinitionService.getPermissions(delegationDefOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		} finally {
			logout();
		}
	}
}
