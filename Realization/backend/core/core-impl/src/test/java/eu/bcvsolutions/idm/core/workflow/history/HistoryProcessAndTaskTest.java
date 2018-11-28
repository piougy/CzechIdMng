package eu.bcvsolutions.idm.core.workflow.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test history of process and tasks
 *
 * @author svandav
 */
@SuppressWarnings("deprecation")
public class HistoryProcessAndTaskTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testHistoryProcessAndTask";
	private static final String PROCESS_KEY_HISTORY = "testHistoryComponent";

	@Autowired
	private WorkflowHistoricProcessInstanceService historicProcessService;
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;
	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;
	@Autowired
	private WorkflowHistoricTaskInstanceService historicTaskService;
	@Autowired
	private LookupService lookupService;

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
		//Start instance of process
		ProcessInstance instance = processInstanceService.startProcess(PROCESS_KEY, null, InitTestData.TEST_USER_1, null,
				null);
		logout();
		// Log as user without ADMIN rights
		loginAsNoAdmin(InitTestData.TEST_USER_1);
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(instance.getId());
		ResourcesWrapper<WorkflowProcessInstanceDto> processes = processInstanceService.search(filter);

		assertEquals(PROCESS_KEY, ((List<WorkflowProcessInstanceDto>) processes.getResources()).get(0).getName());
		WorkflowHistoricProcessInstanceDto historicProcessDto = historicProcessService.get(instance.getId());
		assertNotNull(historicProcessDto);

		this.logout();
		// Log as user without ADMIN rights
		loginAsNoAdmin(InitTestData.TEST_USER_2);
		// Applicant for this process is testUser1. For testUser2 must be result
		// null
		historicProcessDto = historicProcessService.get(instance.getId());
		assertNull(historicProcessDto);

		this.logout();
		// Log as ADMIN
		loginAsAdmin(InitTestData.TEST_USER_2);
		// Applicant for this process is testUser1. For testUser2 must be result
		// null, but as ADMIN can see all historic processes
		historicProcessDto = historicProcessService.get(instance.getId());
		assertNotNull(historicProcessDto);

		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_1);

		completeTasksAndCheckHistory();
	}


	private void completeTasksAndCheckHistory() {

		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessDefinitionKey(PROCESS_KEY);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(1, tasks.size());
		assertEquals("userTaskFirst", tasks.get(0).getName());
		String taskId = tasks.get(0).getId();
		String processId = tasks.get(0).getProcessInstanceId();

		taskInstanceService.completeTask(taskId, null);

		//Check task history
		checkTaskHistory(taskId, InitTestData.TEST_USER_1);

		//Second task is for testUser2 (is candidate) for testUser1 must be null
		filter.setCandidateOrAssigned(InitTestData.TEST_USER_1);
		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(0, tasks.size());

		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		filter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(1, tasks.size());
		assertEquals("userTaskSecond", tasks.get(0).getName());
		taskId = tasks.get(0).getId();
		taskInstanceService.completeTask(taskId, null);

		//Check task history
		checkTaskHistory(taskId, InitTestData.TEST_USER_2);

		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(0, tasks.size());

		//Find history of process. Historic process must exist and must be ended.
		WorkflowHistoricProcessInstanceDto historicProcess = historicProcessService.get(processId);
		assertNotNull(historicProcess);
		assertNotNull(historicProcess.getEndTime());

	}

	/**
	 * Check task history
	 *
	 * @param taskId
	 */
	private void checkTaskHistory(String taskId, String assignee) {
		IdmIdentityDto assigneeIdentity = (IdmIdentityDto) lookupService.getDtoLookup(IdmIdentityDto.class).lookup(assignee);
		WorkflowHistoricTaskInstanceDto taskHistory = historicTaskService.get(taskId);
		assertNotNull(taskHistory);
		assertEquals("completed", taskHistory.getDeleteReason());
		assertEquals(assigneeIdentity.getId().toString(), taskHistory.getAssignee());
		assertEquals(taskId, taskHistory.getId());
	}

	@Test
	public void getHistoryForWf() {
		ProcessInstance instance = processInstanceService.startProcess(PROCESS_KEY_HISTORY, null, InitTestData.TEST_USER_1, null,
				null);
		Assert.assertNotNull(instance);

		checkHistory(instance, 0);
		approveTask(instance);
		checkHistory(instance, 1);
	}

	private void checkHistory(ProcessInstance instance, int size) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(instance.getProcessInstanceId());
		List<WorkflowTaskInstanceDto> history = taskInstanceService.find(filter, null).getContent();
		Assert.assertEquals(1, history.size());
		Assert.assertFalse(history.get(0).getFormData().isEmpty());

		FormDataDto historyData = null;
		for (FormDataDto data : history.get(0).getFormData()) {
			if (data.getId().equals("history")) {
				historyData = data;
			}
		}
		Assert.assertNotNull(historyData);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getDeserializationConfig().addMixInAnnotations(WorkflowHistoricTaskInstanceDto.class, IgnoreSetIdMixIn.class);
		List<WorkflowHistoricTaskInstanceDto> values = null;
		try {
			values = objectMapper.readValue(historyData.getValue(), new TypeReference<List<WorkflowHistoricTaskInstanceDto>>(){});
		} catch (IOException e) {
			throw new CoreException(e);
		}
		Assert.assertNotNull(values);
		Assert.assertEquals(size, values.size());
	}

	private void approveTask(ProcessInstance instance) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(instance.getProcessInstanceId());
		List<WorkflowTaskInstanceDto> tasks = taskInstanceService.find(filter, null).getContent();
		assertEquals(1, tasks.size());
		String taskId = tasks.get(0).getId();
		taskInstanceService.completeTask(taskId, "approve");
	}

	/**
	 * Its needed for parsing history from string
	 */
	abstract class IgnoreSetIdMixIn
	{
		@JsonProperty("id")
		@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="class")
		public abstract void setId(String id);
	}
}
