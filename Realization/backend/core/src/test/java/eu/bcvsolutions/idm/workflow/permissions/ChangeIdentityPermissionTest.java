package eu.bcvsolutions.idm.workflow.permissions;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractWorkflowTest;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.rest.IdmIdentityController;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.rest.WorkflowTaskInstanceController;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test change permission for identity
 * 
 * @author svandav
 *
 */
public class ChangeIdentityPermissionTest extends AbstractWorkflowTest {

	private static final String PROCESS_KEY = "changeIdentityRoles";

	@Autowired
	private WorkflowHistoricProcessInstanceService historicProcessService;
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;
	@Autowired
	private WorkflowDeploymentService processDeploymentService;
	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;
	@Autowired
	private WorkflowHistoricTaskInstanceService historicTaskService;
	@Autowired
	private IdmIdentityController idmIdentityController;
	@Autowired
	private IdmIdentityRepository idmIdentityRepository;
	@Autowired
	private WorkflowTaskInstanceController workflowTaskInstanceController;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void deployAndRunProcess() {
		//Deploy process
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("eu/bcvsolutions/idm/core/workflow/role/changeIdentityRoles.bpmn20.xml");
		WorkflowDeploymentDto deploymentDto = processDeploymentService.create(PROCESS_KEY, "test.changeIdentityRoles.bpmn20.xml", is);
		assertNotNull(deploymentDto);
		
		//start change role process for TEST_USER_1
		this.loginAsAdmin(InitTestData.TEST_USER_1);
		IdmIdentity test1 = idmIdentityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> createChangeRequestWrapped = idmIdentityController.changePermissions(test1.getId().toString());
		WorkflowTaskInstanceDto createChangeRequest = createChangeRequestWrapped.getBody().getResource();
		assertNotNull(createChangeRequest);
		createChangeRequest.getApplicant();
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		roles.add(createNewPermission("SuperAdminRole", null, null));
		variables.put("addedIdentityRoles", roles);
		variables.put("changedIdentityRoles", Lists.newArrayList());
		variables.put("deletedIdentityRoles", Lists.newArrayList());
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedResult.getBody().getResources().isEmpty());
		
		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedResult.getBody().getResources());
		Assert.isTrue(wrappedResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedResult.getBody().getResources()).get(0).getResource();
		
		//taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		

		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_1);
		
	}

	private Map<String, Object> createNewPermission(String roleName, Date validFrom, Date validTill) {
		Map<String, Object> role = new HashMap<>();
		Map<String, Object> roleEmbedded = new HashMap<>();
		Map<String, Object> roleIdEmbedded = new HashMap<>();
		role.put("validTill", validTill);
		role.put("validFrom", validFrom);
		role.put("_embedded", roleEmbedded.put("role", roleIdEmbedded));
		roleIdEmbedded.put("id", "1");
	
		return role;
	}

}
