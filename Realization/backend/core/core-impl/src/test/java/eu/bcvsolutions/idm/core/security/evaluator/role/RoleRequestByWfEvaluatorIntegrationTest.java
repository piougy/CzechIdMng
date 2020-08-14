package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Test permissions for role requests by identity involved in a WF.
 *
 * @author Vít Švanda
 *
 */
public class RoleRequestByWfEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	private static final String APPROVE_BY_HELPDESK_ROLE = "idm.sec.core.wf.approval.helpdesk.role";
	private static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	private static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmConfigurationService configurationService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private IdmIdentityService identityService;

	@Test
	public void testRoleRequestByWfInvolvedIdentityEvaluator() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");

		loginAsAdmin();
		IdmIdentityDto applicant = getHelper().createIdentity();
		IdmIdentityDto otherUser = getHelper().createIdentity();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto policyRole = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();

		// Create policy with RoleRequestByWfInvolvedIdentityEvaluator.
		IdmAuthorizationPolicyDto roleRequestPolicy = getHelper().createBasePolicy(
				policyRole.getId(), CoreGroupPermission.ROLEREQUEST, IdmRoleRequest.class, IdmBasePermission.ADMIN
		);
		roleRequestPolicy.setEvaluator(RoleRequestByWfInvolvedIdentityEvaluator.class);
		getHelper().getService(IdmAuthorizationPolicyService.class).save(roleRequestPolicy);

		// Assign policy to all our's users.
		getHelper().createIdentityRole(applicant, policyRole);
		getHelper().createIdentityRole(otherUser, policyRole);
		getHelper().createIdentityRole(helpdeskIdentity, policyRole);

		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(applicant.getId());

		loginAsNoAdmin(applicant.getUsername());
		IdmRoleRequestDto request = createRoleRequest(applicant);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		loginAsNoAdmin(otherUser.getUsername());
		try {
			roleRequestService.checkAccess(request, IdmBasePermission.READ);
			fail("This user: " + otherUser.getUsername() + " can't read this role-request");
		} catch (ForbiddenEntityException ex) {
			// OK
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			roleRequestService.checkAccess(request, IdmBasePermission.READ);
		} catch (ResultCodeException ex) {
			fail("This user: " + helpdeskIdentity.getUsername() + " can read this role-request.");
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testHistoricRoleRequestByWfInvolvedIdentityEvaluator() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");

		loginAsAdmin();
		IdmIdentityDto applicant = getHelper().createIdentity();
		IdmIdentityDto otherUser = getHelper().createIdentity();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto policyRole = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();

		// Create policy with RoleRequestByWfInvolvedIdentityEvaluator.
		IdmAuthorizationPolicyDto roleRequestPolicy = getHelper().createBasePolicy(
				policyRole.getId(), CoreGroupPermission.ROLEREQUEST, IdmRoleRequest.class, IdmBasePermission.ADMIN
		);
		roleRequestPolicy.setEvaluator(RoleRequestByWfInvolvedIdentityEvaluator.class);
		getHelper().getService(IdmAuthorizationPolicyService.class).save(roleRequestPolicy);

		// Assign policy to all our's users.
		getHelper().createIdentityRole(applicant, policyRole);
		getHelper().createIdentityRole(otherUser, policyRole);
		getHelper().createIdentityRole(helpdeskIdentity, policyRole);

		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(applicant.getId());

		loginAsNoAdmin(applicant.getUsername());
		IdmRoleRequestDto request = createRoleRequest(applicant);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		// Approve the task - process will be ended and move to history.
		loginAsNoAdmin(helpdeskIdentity.getUsername());
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setProcessInstanceId(request.getWfProcessId());
		this.checkAndCompleteOneTask(taskFilter, applicant.getUsername(), "approve", null);

		loginAsNoAdmin(otherUser.getUsername());
		try {
			roleRequestService.checkAccess(request, IdmBasePermission.READ);
			fail("This user: " + otherUser.getUsername() + " can't read this role-request");
		} catch (ForbiddenEntityException ex) {
			// OK
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			roleRequestService.checkAccess(request, IdmBasePermission.READ);
		} catch (ResultCodeException ex) {
			fail("This user: " + helpdeskIdentity.getUsername() + " can read this role-request.");
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	private IdmConceptRoleRequestDto createRoleConcept(IdmRoleDto adminRole, IdmIdentityContractDto contract,
			IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(adminRole.getId());
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	private IdmRoleRequestDto createRoleRequest(IdmIdentityDto test1) {
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(test1.getId());
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		return request;
	}

	private void checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String user, String decision, String userTaskId) {
		IdmIdentityDto identity = identityService.getByUsername(user);
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		if (userTaskId != null) {
			assertEquals(userTaskId, tasks.get(0).getDefinition().getId());
		}
		assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());

		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
	}
}
