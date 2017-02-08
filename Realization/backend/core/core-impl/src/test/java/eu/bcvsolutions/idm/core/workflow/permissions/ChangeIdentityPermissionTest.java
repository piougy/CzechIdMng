package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.rest.impl.IdmIdentityController;
import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.rest.WorkflowTaskInstanceController;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test change permissions for identity
 * 
 * @author svandav
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChangeIdentityPermissionTest extends AbstractWorkflowIntegrationTest {

	private static final String ADDED_IDENTITY_ROLES_VARIABLE = "addedIdentityRoles";
	private static final String REMOVED_IDENTITY_ROLES_VARIABLE = "removedIdentityRoles";
	private static final String CHANGED_IDENTITY_ROLES_VARIABLE = "changedIdentityRoles";

	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;
	@Autowired
	private IdmIdentityController idmIdentityController;
	@Autowired
	private IdmIdentityRepository idmIdentityRepository;
	@Autowired
	private IdmRoleRepository idmRoleRepository;
	@Autowired
	private IdmIdentityRoleRepository idmIdentityRoleRepository;
	@Autowired
	private WorkflowTaskInstanceController workflowTaskInstanceController;
	private DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyy-MM-dd");

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void addApprovableSuperAdminRole() {
		IdmIdentity test1;
		Page<IdmIdentityRole> idmIdentityRolePage;
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE, false);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		roles.add(createNewPermission(InitTestData.TEST_ADMIN_ROLE, null, null));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/workflow/role/approve/approveRoleBySuperAdminRole.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found one task (because was started subprocess WF for approve add permission for all users with SuperAdminRole)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByAdmin = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Approve add permission by admin
		taskInstanceService.completeTask(approveByAdmin.getId(), "approve", null, null);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found no any task
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		test1 = idmIdentityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		idmIdentityRolePage = idmIdentityRoleRepository.findByIdentity(test1, null);
		final List<IdmIdentityRole> idmIdentityRoleList2 = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList2.add(s));
		//User test 1 must have superAdminRole
		IdmIdentityRole idmIdentityRole2 = idmIdentityRoleList2.stream().filter(s -> {return s.getRole().getName().equals(InitTestData.TEST_ADMIN_ROLE);}).findFirst().get();
		// Original creator must be equal with applicant
		assertEquals("Original creator must be equal with applicant", InitTestData.TEST_USER_1, idmIdentityRole2.getOriginalCreator());	
	}
	
	@Test
	public void changeApprovableSuperAdminRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE, true);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		LocalDate localDate = new LocalDate();
		LocalDate validFrom = localDate.minusMonths(1);
		LocalDate validTill = localDate.plusMonths(1);
		IdmIdentityRole superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		
		//Validity date form and till must be null 
		Assert.assertNull(superAdminPermission.getValidFrom());
		Assert.assertNull(superAdminPermission.getValidTill());
		
		roles.add(createChangePermission(superAdminPermission.getId(), validFrom, validTill));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/workflow/role/approve/approveRoleBySuperAdminRole.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found one task (because was started subprocess WF for approve add permission for all users with SuperAdminRole)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByAdmin = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Approve add permission by admin
		taskInstanceService.completeTask(approveByAdmin.getId(), "approve", null, null);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found no any task
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		//Validity date form and till must be not null 
		assertNotNull(superAdminPermission.getValidFrom());
		assertNotNull(superAdminPermission.getValidTill());
		// Validity date must be same as required validity on start of this test 
		Assert.assertTrue(sdf.print(superAdminPermission.getValidFrom()).equals(sdf.print(validFrom)));
		Assert.assertTrue(sdf.print(superAdminPermission.getValidTill()).equals(sdf.print(validTill)));
		//Original modifier must be equal with applicant
		Assert.assertTrue( "Original modifier must be equal with applicant", InitTestData.TEST_USER_1.equals(superAdminPermission.getOriginalModifier()));
		
	}
	
	@Test
	public void changeNotApprovableUserRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE, true);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		LocalDate localDate = new LocalDate();
		LocalDate validFrom = localDate.minusMonths(1);
		LocalDate validTill = localDate.plusMonths(1);
		IdmIdentityRole userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		
		//Validity date form and till must be null 
		Assert.assertNull(userRolePermission.getValidFrom());
		Assert.assertNull(userRolePermission.getValidTill());
		
		roles.add(createChangePermission(userRolePermission.getId(), validFrom, validTill));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/workflow/role/notapprove/notApproveRoleRealizationUpdate.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approved task)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		//Validity date form and till must be not null 
		assertNotNull(userRolePermission.getValidFrom());
		assertNotNull(userRolePermission.getValidTill());
		// Validity date must be same as required validity on start of this test 
		Assert.assertTrue(sdf.print(userRolePermission.getValidFrom()).equals(sdf.print(validFrom)));
		Assert.assertTrue(sdf.print(userRolePermission.getValidTill()).equals(sdf.print(validTill)));
		//Original modifier must be equal with applicant
		Assert.assertTrue( "Original modifier must be equal with applicant", InitTestData.TEST_USER_1.equals(userRolePermission.getOriginalModifier()));
		
		
	}
	
	@Test
	public void addNotApprovableUserRole() {
		IdmIdentity test1;
		Page<IdmIdentityRole> idmIdentityRolePage;
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE, false);
		assertNotNull(createChangeRequest);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		roles.add(createNewPermission(InitTestData.TEST_USER_ROLE, null, null));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess (without approving)
		WorkflowDeploymentDto deploymentDtoNotApprove = deployProcess("eu/bcvsolutions/idm/workflow/role/notapprove/notApproveRoleRealizationAdd.bpmn20.xml");
		assertNotNull(deploymentDtoNotApprove);
		
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_USER_1);
		
		//UserRole is no approvable, therefore user test 1 must have userRole without any additional approving
		test1 = idmIdentityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		idmIdentityRolePage = idmIdentityRoleRepository.findByIdentity(test1, null);
		final List<IdmIdentityRole> idmIdentityRoleList2 = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList2.add(s));
		//User test 1 must have user role
		IdmIdentityRole idmIdentityRole2 = idmIdentityRoleList2.stream().filter(s -> {return s.getRole().getName().equals(InitTestData.TEST_USER_ROLE);}).findFirst().get();
		Assert.assertNotNull(idmIdentityRole2);
		// Original creator must be equal with applicant
		Assert.assertTrue( "Original creator must be equal with applicant", InitTestData.TEST_USER_1.equals(idmIdentityRole2.getOriginalCreator()));
	}
	
	@Test
	public void removeApprovableSuperAdminRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE, true);
		List<UUID> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		IdmIdentityRole superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		// Add permission ID to remove list
		roles.add(superAdminPermission.getId());
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, roles);
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/workflow/role/approve/approveRemoveRoleBySuperAdminRole.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found one task (because was started subprocess WF for approve add permission for all users with SuperAdminRole)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByAdmin = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Approve add permission by admin
		taskInstanceService.completeTask(approveByAdmin.getId(), "approve", null, null);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found no any task
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		Assert.assertNull(superAdminPermission);
		
	}
	
	@Test
	public void removeNotApprovableUserRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE, true);
		List<UUID> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		IdmIdentityRole userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		// Add permission ID to remove list
		roles.add(userRolePermission.getId());
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, roles);
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/workflow/role/notapprove/notApproveRoleRealizationRemove.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.assertTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		Assert.assertNull(userRolePermission);
		
	}
	
	
	private IdmIdentityRole getPermission(String user, String roleName) {
		Page<IdmIdentityRole> idmIdentityRolePage = idmIdentityRoleRepository.findByIdentityUsername(user, null);
		final List<IdmIdentityRole> idmIdentityRoleList = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList.add(s));
		IdmIdentityRole superAdminPermission = null;
		try{
			superAdminPermission = idmIdentityRoleList.stream().filter(s -> {return s.getRole().getName().equals(roleName);}).findFirst().get();
		}catch(NoSuchElementException ex){
			return null;
		}
		return superAdminPermission;
	}

	private WorkflowTaskInstanceDto startChangePermissions(String user, String role, boolean mustHaveRole) {
		//Deploy process
		WorkflowDeploymentDto deploymentDto = deployProcess("eu/bcvsolutions/idm/workflow/role/changeIdentityRoles.bpmn20.xml");
		assertNotNull(deploymentDto);
		
		//start change role process for TEST_USER_1
		this.loginAsAdmin(user);
		IdmIdentity test1 = idmIdentityRepository.findOneByUsername(user);
		
		Page<IdmIdentityRole> idmIdentityRolePage = idmIdentityRoleRepository.findByIdentity(test1, null);
		final List<IdmIdentityRole> idmIdentityRoleList = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList.add(s));
		//User test 1 don't have superAdminRole yet
		boolean rolePresent = idmIdentityRoleList.stream().filter(s -> {return s.getRole().getName().equals(role);}).findFirst().isPresent();
		Assert.assertTrue(mustHaveRole ? rolePresent : !rolePresent);
		
		ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> createChangeRequestWrapped = idmIdentityController.changePermissions(test1.getId().toString());
		WorkflowTaskInstanceDto createChangeRequest = createChangeRequestWrapped.getBody().getResource();
		return createChangeRequest;
	}

	private Map<String, Object> createNewPermission(String roleName, LocalDate validFrom, LocalDate validTill) {
		Map<String, Object> role = new HashMap<>();
		Map<String, Object> roleEmbedded = new HashMap<>();
		Map<String, Object> roleIdEmbedded = new HashMap<>();
		roleEmbedded.put("role", roleIdEmbedded);
		roleIdEmbedded.put("id", idmRoleRepository.findOneByName(roleName).getId());
		role.put("validTill", validTill == null ? "" : sdf.print(validTill));
		role.put("validFrom", validFrom == null ? "" : sdf.print(validFrom));
		role.put("_embedded", roleEmbedded);
	
		return role;
	}
	
	private Map<String, Object> createChangePermission(UUID identityRoleId, LocalDate validFrom, LocalDate validTill) {
		Map<String, Object> role = new HashMap<>();
		
		role.put("validTill", validTill == null ? "" : sdf.print(validTill));
		role.put("validFrom", validFrom == null ? "" : sdf.print(validFrom));
		role.put("id", identityRoleId);
	
		return role;
	}

}
