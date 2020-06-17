package eu.bcvsolutions.idm.core.delegation;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.delegation.type.ApproveRoleByManagerDelegationType;
import eu.bcvsolutions.idm.core.model.delegation.type.DefaultDelegationType;
import eu.bcvsolutions.idm.core.workflow.model.dto.IdentityLinkDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.permissions.ChangeIdentityPermissionTest;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.activiti.engine.task.IdentityLinkType;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Delegation integration test
 *
 * @author Vít Švanda
 *
 */
public class DelegationIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private DelegationManager delegationManager;
	@Autowired
	private IdmDelegationDefinitionService delegationDefinitionService;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmDelegationService delegationService;

	@Before
	public void login() {
		loginAsAdmin();
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_HELPDESK_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_USERMANAGER_ENABLE, false);
	}

	@After
	@Override
	public void logout() {
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_HELPDESK_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_USERMANAGER_ENABLE, false);
		//
		super.logout();
	}

	/**
	 * Green line test.
	 */
	@Test
	public void testDefaultTaskDelegation() {

		// Enable approving of a role-request by manager.
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, true);

		IdmRoleDto roleOne = getHelper().createRole();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setManagersByContract(subordinateContract.getId());

		// Check if delegator is manager for subordinate.
		boolean delegatorIsManager = getHelper().getService(IdmIdentityService.class)
				.find(identityFilter, null)
				.getContent()
				.stream()
				.filter(identity -> identity.getId().equals(delegator.getId()))
				.findFirst()
				.isPresent();
		assertTrue(delegatorIsManager);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		// Create default delegation.
		IdmDelegationDefinitionDto definition = new IdmDelegationDefinitionDto();
		definition.setType(DefaultDelegationType.NAME);
		definition.setDelegator(delegator.getId());
		definition.setDelegate(delegate.getId());
		delegationDefinitionService.save(definition);

		// Delete previous role-request.
		roleRequestService.delete(roleRequest);

		// Assing role -> reuqest will be in progress state.
		roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);
		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		// Should be 0, because task is not assigned to the delegator anymore.
		assertEquals(0, tasks.size());

		// Login as delegate.
		taskFilter.setCandidateOrAssigned(delegate.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(2, identityLinks.size());
		// Task have to be assigned to the delegate now.
		assertEquals(delegate.getId(), UUID.fromString(identityLinks.stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
				.findFirst()
				.get()
				.getUserId()));
		// Task must contains original delegator as prticipant (right to the task in the audit).
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.stream()
				.filter(identityLink -> IdentityLinkType.PARTICIPANT.equals(identityLink.getType()))
				.findFirst()
				.get()
				.getUserId()));
	}

	@Test
	public void testDefaultTaskDelegationForMoreDelegates() {

		// Enable approving of a role-request by manager.
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, true);

		IdmRoleDto roleOne = getHelper().createRole();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegateOne = getHelper().createIdentity();
		IdmIdentityDto delegateTwo = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setManagersByContract(subordinateContract.getId());

		// Check if delegator is manager for subordinate.
		boolean delegatorIsManager = getHelper().getService(IdmIdentityService.class)
				.find(identityFilter, null)
				.getContent()
				.stream()
				.filter(identity -> identity.getId().equals(delegator.getId()))
				.findFirst()
				.isPresent();
		assertTrue(delegatorIsManager);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		// Create default delegations.
		IdmDelegationDefinitionDto definition = new IdmDelegationDefinitionDto();
		definition.setType(DefaultDelegationType.NAME);
		definition.setDelegator(delegator.getId());
		definition.setDelegate(delegateOne.getId());
		delegationDefinitionService.save(definition);

		definition = new IdmDelegationDefinitionDto();
		definition.setType(DefaultDelegationType.NAME);
		definition.setDelegator(delegator.getId());
		definition.setDelegate(delegateTwo.getId());
		delegationDefinitionService.save(definition);

		// Delete previous role-request.
		roleRequestService.delete(roleRequest);

		// Assing role -> reuqest will be in progress state.
		roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);
		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		// Should be 0, because task is not assigned to the delegator anymore.
		assertEquals(0, tasks.size());

		// Login as delegate.
		taskFilter.setCandidateOrAssigned(delegateOne.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		taskFilter.setCandidateOrAssigned(delegateTwo.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());

		identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(3, identityLinks.size());
		// Task have to be assigned to the delegate One now.
		assertEquals(1, identityLinks.stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
				.filter(identityLink -> delegateOne.getId().equals(UUID.fromString(identityLink.getUserId())))
				.count());
		// Task have to be assigned also to the delegate Two.
		assertEquals(1, identityLinks.stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
				.filter(identityLink -> delegateOne.getId().equals(UUID.fromString(identityLink.getUserId())))
				.count());
	}

	@Test
	public void testApprovingByRoleDirectManagerDelegation() {

		int priorityForApprovingByManager = 222;

		getHelper().setConfigurationValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priorityForApprovingByManager,
				ChangeIdentityPermissionTest.APPROVE_ROLE_BY_MANAGER_KEY);

		IdmRoleDto role = getHelper().createRole(priorityForApprovingByManager);
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityContractDto delegatorContract = getHelper().createIdentityContact(delegator);
		IdmIdentityDto delegateOne = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setManagersByContract(subordinateContract.getId());

		// Check if delegator is manager for subordinate.
		boolean delegatorIsManager = getHelper().getService(IdmIdentityService.class)
				.find(identityFilter, null)
				.getContent()
				.stream()
				.filter(identity -> identity.getId().equals(delegator.getId()))
				.findFirst()
				.isPresent();
		assertTrue(delegatorIsManager);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, role);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		// Create delegation for approving by role manager.
		IdmDelegationDefinitionDto definition = new IdmDelegationDefinitionDto();
		definition.setType(ApproveRoleByManagerDelegationType.NAME);
		definition.setDelegator(delegator.getId());
		definition.setDelegatorContract(delegatorContract.getId());
		definition.setDelegate(delegateOne.getId());
		delegationDefinitionService.save(definition);

		// Delete previous role-request.
		roleRequestService.delete(roleRequest);

		// Assing role -> reuqest will be in progress state.
		roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, role);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);
		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		// Should be 0, because task is not assigned to the delegator anymore.
		assertEquals(0, tasks.size());

		// Login as delegate.
		taskFilter.setCandidateOrAssigned(delegateOne.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(2, identityLinks.size());
		// Task have to be assigned to the delegate now.
		assertEquals(delegateOne.getId(), UUID.fromString(identityLinks.stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType()))
				.findFirst()
				.get()
				.getUserId()));
		// Task must contains original delegator as prticipant (right to the task in the audit).
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.stream()
				.filter(identityLink -> IdentityLinkType.PARTICIPANT.equals(identityLink.getType()))
				.findFirst()
				.get()
				.getUserId()));

		// Set configuration to null.
		getHelper().setConfigurationValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priorityForApprovingByManager,
				null);
	}

	@Test
	public void testDelegationDefinitionValidityDifferentDelegate() {

		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegateOne = getHelper().createIdentity();
		IdmIdentityDto delegateTwo = getHelper().createIdentity();

		// Create default delegations.
		IdmDelegationDefinitionDto definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne = delegationDefinitionService.save(definitionOne);

		IdmDelegationDefinitionDto definitionTwo = new IdmDelegationDefinitionDto();
		definitionTwo.setType(DefaultDelegationType.NAME);
		definitionTwo.setDelegator(delegator.getId());
		definitionTwo.setDelegate(delegateTwo.getId());
		definitionTwo = delegationDefinitionService.save(definitionTwo);

		List<IdmDelegationDefinitionDto> delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// Two delegations are valid for this delegator.
		assertEquals(2, delegations.size());

		// Set definition Two as unvalid.
		delegationDefinitionService.delete(definitionTwo);
		definitionTwo = new IdmDelegationDefinitionDto();
		definitionTwo.setType(DefaultDelegationType.NAME);
		definitionTwo.setDelegator(delegator.getId());
		definitionTwo.setDelegate(delegateTwo.getId());
		definitionTwo.setValidFrom(LocalDate.now().minusDays(10));
		definitionTwo.setValidTill(LocalDate.now().minusDays(1));
		delegationDefinitionService.save(definitionTwo);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// Only delegation One is valid now.
		assertEquals(1, delegations.size());
		assertEquals(definitionOne.getId(), delegations.get(0).getId());

		// Set definition One as unvalid.
		delegationDefinitionService.delete(definitionOne);
		definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne.setValidFrom(LocalDate.now().plusDays(20));
		definitionOne.setValidTill(LocalDate.now().plusDays(10));
		definitionOne = delegationDefinitionService.save(definitionOne);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// No delegation is valid now.
		Assert.assertNull(delegations);

		// Set definition One valid in future.
		delegationDefinitionService.delete(definitionOne);
		definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne.setValidFrom(LocalDate.now().plusDays(20));
		definitionOne.setValidTill(null);
		definitionOne = delegationDefinitionService.save(definitionOne);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// No delegation is valid now.
		Assert.assertNull(delegations);

		// Set definition One as valid.
		delegationDefinitionService.delete(definitionOne);
		definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne.setValidFrom(LocalDate.now());
		definitionOne.setValidTill(LocalDate.now().plusDays(10));
		definitionOne = delegationDefinitionService.save(definitionOne);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// Only delegation One is valid now.
		assertEquals(1, delegations.size());
		assertEquals(definitionOne.getId(), delegations.get(0).getId());
	}

	@Test
	public void testDelegationDefinitionValiditySameDelegate() {

		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegateOne = getHelper().createIdentity();

		// Create default delegations.
		IdmDelegationDefinitionDto definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne = delegationDefinitionService.save(definitionOne);

		IdmDelegationDefinitionDto definitionTwo = new IdmDelegationDefinitionDto();
		definitionTwo.setType(DefaultDelegationType.NAME);
		definitionTwo.setDelegator(delegator.getId());
		definitionTwo.setDelegate(delegateOne.getId());
		definitionTwo = delegationDefinitionService.save(definitionTwo);

		List<IdmDelegationDefinitionDto> delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// Two delegations are valid for this delegator.
		assertEquals(2, delegations.size());

		// Set definition Two as unvalid.
		delegationDefinitionService.delete(definitionTwo);
		definitionTwo = new IdmDelegationDefinitionDto();
		definitionTwo.setDelegator(delegator.getId());
		definitionTwo.setDelegate(delegateOne.getId());
		definitionTwo.setType(DefaultDelegationType.NAME);
		definitionTwo.setValidFrom(LocalDate.now().minusDays(10));
		definitionTwo.setValidTill(LocalDate.now().minusDays(1));
		delegationDefinitionService.save(definitionTwo);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// Only delegation One is valid now.
		assertEquals(1, delegations.size());
		assertEquals(definitionOne.getId(), delegations.get(0).getId());

		// Set definition One as unvalid.
		delegationDefinitionService.delete(definitionOne);
		definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne.setValidFrom(LocalDate.now().plusDays(20));
		definitionOne.setValidTill(LocalDate.now().plusDays(10));
		definitionOne = delegationDefinitionService.save(definitionOne);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// No delegation is valid now.
		Assert.assertNull(delegations);

		// Set definition One valid in future.
		delegationDefinitionService.delete(definitionOne);
		definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setValidFrom(LocalDate.now().plusDays(20));
		definitionOne.setValidTill(null);
		definitionOne = delegationDefinitionService.save(definitionOne);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// No delegation is valid now.
		Assert.assertNull(delegations);

		// Set definition One as valid.
		delegationDefinitionService.delete(definitionOne);
		definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(DefaultDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne.setValidFrom(LocalDate.now());
		definitionOne.setValidTill(LocalDate.now().plusDays(10));
		definitionOne = delegationDefinitionService.save(definitionOne);

		delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// Only delegation One is valid now.
		assertEquals(1, delegations.size());
		assertEquals(definitionOne.getId(), delegations.get(0).getId());
	}

	@Test
	public void testDelegationDefinitionFindByNotUsedType() {

		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegateOne = getHelper().createIdentity();

		// Create default delegations.
		IdmDelegationDefinitionDto definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(ApproveRoleByManagerDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		delegationDefinitionService.save(definitionOne);

		IdmDelegationDefinitionDto definitionTwo = new IdmDelegationDefinitionDto();
		definitionTwo.setType(ApproveRoleByManagerDelegationType.NAME);
		definitionTwo.setDelegator(delegator.getId());
		definitionTwo.setDelegate(delegateOne.getId());
		delegationDefinitionService.save(definitionTwo);

		List<IdmDelegationDefinitionDto> delegations = delegationManager
				.findDelegation(DefaultDelegationType.NAME, delegator.getId(), null, null);

		// No delegations are valid for this delegator and this type.
		Assert.assertNull(delegations);
	}

	@Test(expected = ResultCodeException.class)
	public void testDelegationCannotBeUpdated() {

		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegateOne = getHelper().createIdentity();

		// Create default delegations.
		IdmDelegationDefinitionDto definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(ApproveRoleByManagerDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne = delegationDefinitionService.save(definitionOne);

		// Update of the delegation should throw exception.
		delegationDefinitionService.save(definitionOne);
	}

	@Test
	public void testDelegationDefinitionIntegrity() {

		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto delegateOne = getHelper().createIdentity();

		// Create default delegations and delegations.
		IdmDelegationDefinitionDto definitionOne = new IdmDelegationDefinitionDto();
		definitionOne.setType(ApproveRoleByManagerDelegationType.NAME);
		definitionOne.setDelegator(delegator.getId());
		definitionOne.setDelegate(delegateOne.getId());
		definitionOne = delegationDefinitionService.save(definitionOne);

		IdmDelegationDto delegationOne = new IdmDelegationDto();
		delegationOne.setDefinition(definitionOne.getId());
		delegationOne.setOwnerId(UUID.randomUUID());
		delegationOne.setOwnerType(WorkflowTaskInstanceDto.class.getCanonicalName());
		delegationOne = delegationService.save(delegationOne);

		IdmDelegationDto delegationTwo = new IdmDelegationDto();
		delegationTwo.setDefinition(definitionOne.getId());
		delegationTwo.setOwnerId(UUID.randomUUID());
		delegationTwo.setOwnerType(WorkflowTaskInstanceDto.class.getCanonicalName());
		delegationTwo = delegationService.save(delegationTwo);

		// Delegations shuld be deleted on delete of the definition.
		delegationDefinitionService.delete(definitionOne);

		IdmDelegationDto delegation = delegationService.get(delegationOne.getId());
		Assert.assertNull(delegation);
		delegation = delegationService.get(delegationTwo.getId());
		Assert.assertNull(delegation);
	}
}
