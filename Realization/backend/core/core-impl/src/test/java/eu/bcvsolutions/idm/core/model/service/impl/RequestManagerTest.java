package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test for universal request agenda
 * 
 * @author svandav
 *
 */
public class RequestManagerTest extends AbstractCoreWorkflowIntegrationTest {

	@Autowired
	protected TestHelper helper;
	@Autowired
	private RequestManager<Requestable> requestManager;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private IdmRequestService requestService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testCreateRoleByRequest() {

		IdmRoleDto newRole = new IdmRoleDto();
		newRole.setCode(getHelper().createName());
		newRole.setName(newRole.getCode());
		newRole.setPriority(10);
		newRole.setDescription(getHelper().createName());

		IdmRequestDto request = requestManager.createRequest(newRole);
		Assert.assertNotNull(request);

		Requestable requestable = requestManager.post(request.getId(), newRole);
		Assert.assertNotNull(requestable);
		Assert.assertTrue(requestable instanceof IdmRoleDto);

		IdmRoleDto roleFromRequest = (IdmRoleDto) requestable;
		// Is not same instance
		Assert.assertTrue(newRole != roleFromRequest);
		// Has same values as new role
		Assert.assertEquals(newRole.getCode(), roleFromRequest.getCode());
		Assert.assertEquals(newRole.getName(), roleFromRequest.getName());
		Assert.assertEquals(newRole.getPriority(), roleFromRequest.getPriority());
		Assert.assertEquals(newRole.getDescription(), roleFromRequest.getDescription());

		// Role not exists yet
		Assert.assertNull(roleService.get(roleFromRequest.getId()));

		IdmRequestDto executedRequest = requestManager.executeRequest(request.getId());
		Assert.assertNotNull(executedRequest);
		Assert.assertEquals(RequestState.EXECUTED, executedRequest.getState());

		IdmRoleDto executedRole = roleService.get(roleFromRequest.getId());
		// Role must exists now
		Assert.assertNotNull(executedRole);
		// Has same values as new role
		Assert.assertEquals(newRole.getCode(), executedRole.getCode());
		Assert.assertEquals(newRole.getName(), executedRole.getName());
		Assert.assertEquals(newRole.getPriority(), executedRole.getPriority());
		Assert.assertEquals(newRole.getDescription(), executedRole.getDescription());

	}

	@Test
	public void testChangeRoleByRequest() {

		// Create role
		IdmRoleDto changedRole = getHelper().createRole();

		// Create request
		IdmRequestDto request = requestManager.createRequest(changedRole);
		Assert.assertNotNull(request);
		Assert.assertEquals(request.getOwnerType(), changedRole.getClass().getName());
		Assert.assertEquals(request.getOwnerId(), changedRole.getId());

		// Change role (without save)
		changedRole.setDescription(getHelper().createName());
		changedRole.setPriority(1000);
		// Create request item
		Requestable requestable = requestManager.post(request.getId(), changedRole);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());

		Assert.assertTrue(requestable instanceof IdmRoleDto);
		IdmRoleDto roleFromRequest = (IdmRoleDto) requestable;
		// Is not same instance
		Assert.assertTrue(changedRole != roleFromRequest);
		// Has same values as new role
		Assert.assertEquals(changedRole.getPriority(), roleFromRequest.getPriority());
		Assert.assertEquals(changedRole.getDescription(), roleFromRequest.getDescription());

		IdmRoleDto currentRole = roleService.get(changedRole.getId());
		Assert.assertNotEquals(changedRole.getPriority(), currentRole.getPriority());
		Assert.assertNotEquals(changedRole.getDescription(), currentRole.getDescription());

		// Start request
		IdmRequestDto executedRequest = requestManager.executeRequest(request.getId());
		Assert.assertNotNull(executedRequest);
		Assert.assertEquals(RequestState.EXECUTED, executedRequest.getState());

		IdmRoleDto executedRole = roleService.get(roleFromRequest.getId());
		Assert.assertNotNull(executedRole);
		// Has same values as new role
		Assert.assertEquals(changedRole.getCode(), executedRole.getCode());
		Assert.assertEquals(changedRole.getName(), executedRole.getName());
		Assert.assertEquals(changedRole.getPriority(), executedRole.getPriority());
		Assert.assertEquals(changedRole.getDescription(), executedRole.getDescription());

	}

	@Test
	public void testDeleteRoleByRequest() {

		// Create role
		IdmRoleDto role = getHelper().createRole();

		// Create request
		IdmRequestDto request = requestManager.createRequest(role);
		Assert.assertNotNull(request);
		Assert.assertEquals(request.getOwnerType(), role.getClass().getName());
		Assert.assertEquals(request.getOwnerId(), role.getId());

		// Create request item
		Requestable requestable = requestManager.delete(request.getId(), role);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());
		IdmRequestItemDto requestItem = DtoUtils.getEmbedded((AbstractDto) requestable, Requestable.REQUEST_ITEM_FIELD,
				IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.REMOVE, requestItem.getOperation());

		Assert.assertTrue(requestable instanceof IdmRoleDto);
		IdmRoleDto roleFromRequest = (IdmRoleDto) requestable;

		// Is not deleted yet
		IdmRoleDto currentRole = roleService.get(role.getId());
		Assert.assertNotNull(currentRole);

		// Start request
		IdmRequestDto executedRequest = requestManager.executeRequest(request.getId());
		Assert.assertNotNull(executedRequest);
		Assert.assertEquals(RequestState.EXECUTED, executedRequest.getState());

		// Role have to be deleted
		IdmRoleDto executedRole = roleService.get(roleFromRequest.getId());
		Assert.assertNull(executedRole);
	}

	@Test
	public void testChangeRoleWithGuaranteesApprove() {

		// Create role with guarantee
		IdmIdentityDto guarantee = getHelper().createIdentity();
		IdmRoleDto changedRole = getHelper().createRole();
		getHelper().createRoleGuarantee(changedRole, guarantee);

		// Create request
		IdmRequestDto request = requestManager.createRequest(changedRole);
		Assert.assertNotNull(request);
		Assert.assertEquals(request.getOwnerType(), changedRole.getClass().getName());
		Assert.assertEquals(request.getOwnerId(), changedRole.getId());

		// Change role (without save)
		changedRole.setDescription(getHelper().createName());
		changedRole.setPriority(1000);
		// Create request item
		Requestable requestable = requestManager.post(request.getId(), changedRole);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());

		Assert.assertTrue(requestable instanceof IdmRoleDto);
		IdmRoleDto roleFromRequest = (IdmRoleDto) requestable;
		// Is not same instance
		Assert.assertTrue(changedRole != roleFromRequest);
		// Has same values as new role
		Assert.assertEquals(changedRole.getPriority(), roleFromRequest.getPriority());
		Assert.assertEquals(changedRole.getDescription(), roleFromRequest.getDescription());

		IdmRoleDto currentRole = roleService.get(changedRole.getId());
		Assert.assertNotEquals(changedRole.getPriority(), currentRole.getPriority());
		Assert.assertNotEquals(changedRole.getDescription(), currentRole.getDescription());

		// Start request
		IdmRequestDto executedRequest = requestManager.startRequest(request.getId(), true);
		Assert.assertNotNull(executedRequest);
		// Role has guarantee, approval process must be started
		Assert.assertEquals(RequestState.IN_PROGRESS, executedRequest.getState());

		loginAsNoAdmin(guarantee.getUsername());
		try {
			completeTasksFromUsers(guarantee.getId().toString(), "approve");
		} catch (ResultCodeException e) {
			fail("User has permission to approve task. Error message: " + e.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
		// Reload the request (after approving)
		executedRequest = requestService.get(executedRequest.getId());
		Assert.assertEquals(RequestState.EXECUTED, executedRequest.getState());

		IdmRoleDto executedRole = roleService.get(roleFromRequest.getId());
		// Role must exists now
		Assert.assertNotNull(executedRole);
		// Has same values as new role
		Assert.assertEquals(changedRole.getCode(), executedRole.getCode());
		Assert.assertEquals(changedRole.getName(), executedRole.getName());
		Assert.assertEquals(changedRole.getPriority(), executedRole.getPriority());
		Assert.assertEquals(changedRole.getDescription(), executedRole.getDescription());

	}

	@Test
	public void testChangeRoleWithGuaranteesDisapprov() {

		// Create role with guarantee
		IdmIdentityDto guarantee = getHelper().createIdentity();
		IdmRoleDto changedRole = getHelper().createRole();
		getHelper().createRoleGuarantee(changedRole, guarantee);

		// Create request
		IdmRequestDto request = requestManager.createRequest(changedRole);
		Assert.assertNotNull(request);
		Assert.assertEquals(request.getOwnerType(), changedRole.getClass().getName());
		Assert.assertEquals(request.getOwnerId(), changedRole.getId());

		// Change role (without save)
		changedRole.setDescription(getHelper().createName());
		changedRole.setPriority(1000);
		// Create request item
		Requestable requestable = requestManager.post(request.getId(), changedRole);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());

		Assert.assertTrue(requestable instanceof IdmRoleDto);
		IdmRoleDto roleFromRequest = (IdmRoleDto) requestable;
		// Is not same instance
		Assert.assertTrue(changedRole != roleFromRequest);
		// Has same values as new role
		Assert.assertEquals(changedRole.getPriority(), roleFromRequest.getPriority());
		Assert.assertEquals(changedRole.getDescription(), roleFromRequest.getDescription());

		IdmRoleDto currentRole = roleService.get(changedRole.getId());
		Assert.assertNotEquals(changedRole.getPriority(), currentRole.getPriority());
		Assert.assertNotEquals(changedRole.getDescription(), currentRole.getDescription());

		// Start request
		IdmRequestDto executedRequest = requestManager.startRequest(request.getId(), true);
		Assert.assertNotNull(executedRequest);
		// Role has guarantee, approval process must be started
		Assert.assertEquals(RequestState.IN_PROGRESS, executedRequest.getState());

		loginAsNoAdmin(guarantee.getUsername());
		try {
			completeTasksFromUsers(guarantee.getId().toString(), "disapprove");
		} catch (ResultCodeException e) {
			fail("User has permission to approve task. Error message: " + e.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
		// Reload the request (after disapproving)
		executedRequest = requestService.get(executedRequest.getId());
		Assert.assertEquals(RequestState.DISAPPROVED, executedRequest.getState());

		IdmRoleDto executedRole = roleService.get(roleFromRequest.getId());
		Assert.assertNotNull(executedRole);
		// Has different values as new role
		Assert.assertNotEquals(changedRole.getPriority(), executedRole.getPriority());
		Assert.assertNotEquals(changedRole.getDescription(), executedRole.getDescription());

	}

	@Test
	public void testChangeRoleWithGuaranteesSkipApproving() {

		// Create role with guarantee
		IdmIdentityDto guarantee = getHelper().createIdentity();
		IdmRoleDto changedRole = getHelper().createRole();
		getHelper().createRoleGuarantee(changedRole, guarantee);

		// Create request
		IdmRequestDto request = requestManager.createRequest(changedRole);
		Assert.assertNotNull(request);
		Assert.assertEquals(request.getOwnerType(), changedRole.getClass().getName());
		Assert.assertEquals(request.getOwnerId(), changedRole.getId());

		// Change role (without save)
		changedRole.setDescription(getHelper().createName());
		changedRole.setPriority(1000);
		// Create request item
		Requestable requestable = requestManager.post(request.getId(), changedRole);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());

		Assert.assertTrue(requestable instanceof IdmRoleDto);
		IdmRoleDto roleFromRequest = (IdmRoleDto) requestable;
		// Is not same instance
		Assert.assertTrue(changedRole != roleFromRequest);
		// Has same values as new role
		Assert.assertEquals(changedRole.getPriority(), roleFromRequest.getPriority());
		Assert.assertEquals(changedRole.getDescription(), roleFromRequest.getDescription());

		IdmRoleDto currentRole = roleService.get(changedRole.getId());
		Assert.assertNotEquals(changedRole.getPriority(), currentRole.getPriority());
		Assert.assertNotEquals(changedRole.getDescription(), currentRole.getDescription());

		// Skip approving
		request.setExecuteImmediately(true);
		request = requestService.save(request);

		// Start request
		IdmRequestDto executedRequest = requestManager.startRequest(request.getId(), true);
		Assert.assertNotNull(executedRequest);
		// Role has guarantee, but approval process wasn't started because the request
		// skipped approving.
		Assert.assertEquals(RequestState.EXECUTED, executedRequest.getState());

		IdmRoleDto executedRole = roleService.get(roleFromRequest.getId());
		// Role must exists now
		Assert.assertNotNull(executedRole);
		// Has same values as new role
		Assert.assertEquals(changedRole.getCode(), executedRole.getCode());
		Assert.assertEquals(changedRole.getName(), executedRole.getName());
		Assert.assertEquals(changedRole.getPriority(), executedRole.getPriority());
		Assert.assertEquals(changedRole.getDescription(), executedRole.getDescription());

	}

	@Test
	public void testBasicItemIntegrity() {

		// Create role
		IdmRoleDto role = getHelper().createRole();
		// Create request
		IdmRequestDto request = requestManager.createRequest(role);
		Assert.assertNotNull(request);

		// null -> DELETE
		// Create delete request item
		Requestable requestable = requestManager.delete(request.getId(), role);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());
		IdmRequestItemDto requestItem = DtoUtils.getEmbedded((AbstractDto) requestable, Requestable.REQUEST_ITEM_FIELD,
				IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.REMOVE, requestItem.getOperation());
		Assert.assertTrue(requestable instanceof IdmRoleDto);

		// DELETE -> CHANGE
		// Create change request item
		role.setDescription(getHelper().createName());
		Requestable requestablePost = requestManager.post(request.getId(), requestable);
		IdmRequestItemDto changeRequestItem = DtoUtils.getEmbedded((AbstractDto) requestablePost,
				Requestable.REQUEST_ITEM_FIELD, IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.UPDATE, changeRequestItem.getOperation());
		// In one request can exists only one item for same owner -> change item must be
		// same (updated) as in delete cause.
		Assert.assertEquals(requestItem.getId(), changeRequestItem.getId());

		// CHANGE -> null
		// Create delete request item (again)
		requestable = requestManager.delete(request.getId(), requestablePost);
		Assert.assertNotNull(requestable);
		Assert.assertNull(requestable.getRequestItem());
		// Previous item was deleted
		List<IdmRequestItemDto> items = requestManager.findRequestItems(request.getId(), null);
		Assert.assertEquals(0, items.size());

		// null -> DELETE
		// Create delete request item
		requestable = requestManager.delete(request.getId(), requestable);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());
		requestItem = DtoUtils.getEmbedded((AbstractDto) requestable, Requestable.REQUEST_ITEM_FIELD,
				IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.REMOVE, requestItem.getOperation());

		// DELETE -> null
		// Again delete same DTO. In this situation should be previous item (delete)
		// deleted.
		requestable = requestManager.delete(request.getId(), requestable);
		Assert.assertNotNull(requestable);
		Assert.assertNull(requestable.getRequestItem());
		Assert.assertEquals(0, requestManager.findRequestItems(request.getId(), null).size());

		IdmRoleDto newRole = new IdmRoleDto();
		newRole.setCode(getHelper().createName());
		newRole.setName(newRole.getCode());
		// null -> ADD
		Requestable newRequestable = requestManager.post(request.getId(), newRole);
		Assert.assertNotNull(newRequestable);
		Assert.assertNotNull(newRequestable.getRequestItem());
		IdmRequestItemDto newRequestItem = DtoUtils.getEmbedded((AbstractDto) newRequestable,
				Requestable.REQUEST_ITEM_FIELD, IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.ADD, newRequestItem.getOperation());
		// One item must exists now
		Assert.assertEquals(1, requestManager.findRequestItems(request.getId(), null).size());
		// ADD -> null
		// Delete of DTO, which is not created in DB causes his deleting.
		requestable = requestManager.delete(request.getId(), newRequestable);
		Assert.assertNotNull(requestable);
		Assert.assertNull(requestable.getRequestItem());
		Assert.assertEquals(0, requestManager.findRequestItems(request.getId(), null).size());

	}

	@Test
	public void testDeleteRequestIntegrity() {

		// Log as admin, but not user 'admin' (we don't want to skip WF)
		IdmIdentityDto adminUser = getHelper().createIdentity();
		loginAsAdmin(adminUser.getUsername());

		// Create role
		IdmRoleDto role = getHelper().createRole();
		// Create request
		IdmRequestDto request = requestManager.createRequest(role);
		Assert.assertNotNull(request);

		// Create guarantee
		IdmIdentityDto guarantee = getHelper().createIdentity();
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(guarantee.getId());
		Requestable requestablePost = requestManager.post(request.getId(), roleGuarantee);
		IdmRequestItemDto changeRequestItem = DtoUtils.getEmbedded((AbstractDto) requestablePost,
				Requestable.REQUEST_ITEM_FIELD, IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.ADD, changeRequestItem.getOperation());
		Assert.assertTrue(requestablePost instanceof IdmRoleGuaranteeDto);

		// Change role (without save)
		role.setDescription(getHelper().createName());
		role.setPriority(1000);
		// Create request item
		Requestable requestable = requestManager.post(request.getId(), role);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());
		changeRequestItem = DtoUtils.getEmbedded((AbstractDto) requestable, Requestable.REQUEST_ITEM_FIELD,
				IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.UPDATE, changeRequestItem.getOperation());
		Assert.assertTrue(requestable instanceof IdmRoleDto);
		// Request should be in concept state
		request = requestService.get(request.getId());
		Assert.assertEquals(RequestState.CONCEPT, request.getState());

		// Two items should be created
		Assert.assertEquals(2, requestManager.findRequestItems(request.getId(), null).size());

		// Delete the request
		requestService.delete(request);
		IdmRequestDto requestDeleted = requestService.get(request.getId());
		Assert.assertNull(requestDeleted);

		// All items should be deleted
		Assert.assertEquals(0, requestManager.findRequestItems(request.getId(), null).size());
	}

	@Test
	public void testDeleteRequestIntegrityWithWf() {

		// Log as admin, but not user 'admin' (we don't want to skip WF)
		IdmIdentityDto adminUser = getHelper().createIdentity();
		loginAsAdmin(adminUser.getUsername());

		// Create role
		IdmRoleDto role = getHelper().createRole();
		// Create request
		IdmRequestDto request = requestManager.createRequest(role);
		Assert.assertNotNull(request);

		// Create guarantee
		IdmIdentityDto guarantee = getHelper().createIdentity();
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(guarantee.getId());
		Requestable requestablePost = requestManager.post(request.getId(), roleGuarantee);
		IdmRequestItemDto changeRequestItem = DtoUtils.getEmbedded((AbstractDto) requestablePost,
				Requestable.REQUEST_ITEM_FIELD, IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.ADD, changeRequestItem.getOperation());
		Assert.assertTrue(requestablePost instanceof IdmRoleGuaranteeDto);

		// Change role (without save)
		role.setDescription(getHelper().createName());
		role.setPriority(1000);
		// Create request item
		Requestable requestable = requestManager.post(request.getId(), role);
		Assert.assertNotNull(requestable);
		Assert.assertNotNull(requestable.getRequestItem());
		changeRequestItem = DtoUtils.getEmbedded((AbstractDto) requestable, Requestable.REQUEST_ITEM_FIELD,
				IdmRequestItemDto.class);
		Assert.assertEquals(RequestOperationType.UPDATE, changeRequestItem.getOperation());
		Assert.assertTrue(requestable instanceof IdmRoleDto);
		// Start request
		IdmRequestDto executedRequest = requestManager.startRequest(request.getId(), true);
		executedRequest = requestService.get(executedRequest.getId());
		Assert.assertEquals(RequestState.IN_PROGRESS, executedRequest.getState());
		String processId = executedRequest.getWfProcessId();
		Assert.assertNotNull(processId);
		// Wf process is in progress
		WorkflowProcessInstanceDto processInstace  = workflowProcessInstanceService.get(processId);
		Assert.assertNotNull(processInstace);

		// Two items should be created
		Assert.assertEquals(2, requestManager.findRequestItems(request.getId(), null).size());
		// Delete the request
		requestService.delete(executedRequest);
		IdmRequestDto requestDeleted = requestService.get(executedRequest.getId());
		Assert.assertNull(requestDeleted);
		// Process should be deleted (canceled)
		processInstace  = workflowProcessInstanceService.get(processId);
		Assert.assertNull(processInstace);

	}

	/**
	 * Complete all tasks from user given in parameters. Complete will be done by
	 * currently logged user.
	 * 
	 * @param approverUser
	 * @param decision
	 */
	private void completeTasksFromUsers(String approverUser, String decision) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(approverUser);
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		//
		for (WorkflowTaskInstanceDto task : tasks) {
			workflowTaskInstanceService.completeTask(task.getId(), decision);
		}
	}
}
