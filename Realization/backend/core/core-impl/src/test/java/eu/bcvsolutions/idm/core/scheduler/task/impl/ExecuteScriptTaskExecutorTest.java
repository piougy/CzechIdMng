package eu.bcvsolutions.idm.core.scheduler.task.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultLongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for execute script by long running task
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ExecuteScriptTaskExecutorTest extends AbstractIntegrationTest {

	@Autowired
	private ExecuteScriptTaskExecutor executeScriptEvaluator;
	
	@Autowired
	private IdmScriptService scriptService;
	
	@Autowired
	private IdmScriptAuthorityService scriptAuthorityService;
	
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private ExecuteScriptTaskExecutor taskExecutor;
	
	@Autowired
	private ApplicationContext context;
	
	private LongRunningTaskManager manager;
	
	private String TEST_SCRIPT_CODE = "test-executor-script-code";
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultLongRunningTaskManager.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testExecuteScriptDefault() {
		IdmScriptDto scriptDto = new IdmScriptDto();
		scriptDto.setCode(TEST_SCRIPT_CODE + "_1");
		scriptDto.setName(TEST_SCRIPT_CODE + "_1");
		scriptDto.setCategory(IdmScriptCategory.DEFAULT);
		
		StringBuilder builder = new StringBuilder();
		builder.append("task.setCounter(0l);\n");
		builder.append("task.setCount(10l);\n");
		builder.append("for (int index = 0; index < 10; index++) {\n");
		builder.append("    task.increaseCounter();\n");
		builder.append("    task.updateState();\n");
		builder.append("}\n");
		
		scriptDto.setScript(builder.toString());
		
		scriptDto = scriptService.save(scriptDto);
		
		prepareAuthForTestScript(scriptDto);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("scriptCode", TEST_SCRIPT_CODE + "_1");
		executeScriptEvaluator.init(parameters);
		executeScriptEvaluator.process();
	}
	
	@Test
	public void testExecuteScriptSystem() {
		IdmScriptDto scriptDto = new IdmScriptDto();
		scriptDto.setCode(TEST_SCRIPT_CODE + "_2");
		scriptDto.setName(TEST_SCRIPT_CODE + "_2");
		scriptDto.setCategory(IdmScriptCategory.SYSTEM);
		
		StringBuilder builder = new StringBuilder();
		builder.append("task.setCounter(0l);\n");
		builder.append("task.setCount(10l);\n");
		builder.append("for (int index = 0; index < 10; index++) {\n");
		builder.append("    task.increaseCounter();\n");
		builder.append("    task.updateState();\n");
		builder.append("}\n");
		
		scriptDto.setScript(builder.toString());
		
		scriptDto = scriptService.save(scriptDto);
		
		prepareAuthForTestScript(scriptDto);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("scriptCode", TEST_SCRIPT_CODE + "_2");
		executeScriptEvaluator.init(parameters);
		executeScriptEvaluator.process();
	}
	
	@Test(expected = ResultCodeException.class)
	public void testExecuteScriptNonexists() {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("scriptCode", UUID.randomUUID().toString());
		executeScriptEvaluator.init(parameters);
		executeScriptEvaluator.process();
		fail();
	}
	
	@Test
	public void testExeciteScriptWithResult() {
		// remove previous long running task
		List<IdmLongRunningTaskDto> tasks = longRunningTaskService.find(null).getContent();
		for (IdmLongRunningTaskDto task : tasks) {
			longRunningTaskService.delete(task);			
		}
		
		IdmScriptDto scriptDto = new IdmScriptDto();
		scriptDto.setCode(TEST_SCRIPT_CODE + "_3");
		scriptDto.setName(TEST_SCRIPT_CODE + "_3");
		scriptDto.setCategory(IdmScriptCategory.SYSTEM);
		
		StringBuilder builder = new StringBuilder();
		builder.append("import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;\n");
		builder.append("task.setCounter(0l);\n");
		builder.append("task.setCount(5l);\n");
		builder.append("for (int index = 0; index < 5; index++) {\n");
		builder.append("    IdmIdentityDto dto = new IdmIdentityDto();\n");
		builder.append("    dto.setUsername('test-execute-' + index);\n");
		builder.append("    dto.setLastName('test-execute-' + index);\n");
		builder.append("    dto.setFirstName('' + index);\n");
		builder.append("    identityService.save(dto);\n");
		builder.append("    task.increaseCounter();\n");
		builder.append("    task.updateState();\n");
		builder.append("}\n");
		
		scriptDto.setScript(builder.toString());
		
		scriptDto = scriptService.save(scriptDto);	
		
		prepareAuthForTestScript(scriptDto);
		IdmScriptAuthorityDto authDto = new IdmScriptAuthorityDto();
		authDto.setType(ScriptAuthorityType.SERVICE);
		authDto.setClassName("eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService");
		authDto.setScript(scriptDto.getId());
		authDto.setService("identityService");
		scriptAuthorityService.save(authDto);
		
		authDto = new IdmScriptAuthorityDto();
		authDto.setType(ScriptAuthorityType.CLASS_NAME);
		authDto.setClassName("eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto");
		authDto.setScript(scriptDto.getId());
		scriptAuthorityService.save(authDto);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("scriptCode", TEST_SCRIPT_CODE + "_3");
		
		taskExecutor.init(parameters);

		LongRunningFutureTask<Boolean> futureTask = manager.execute(taskExecutor);
		try {
			assertEquals(Boolean.TRUE,  futureTask.getFutureTask().get());
		} catch (InterruptedException | ExecutionException e) {
			fail(e.getMessage());
		}
		IdmLongRunningTaskDto longRunningTask = longRunningTaskService.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
		
		
		assertEquals(5, longRunningTask.getCount().longValue());
		assertEquals(5, longRunningTask.getCounter().longValue());
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setText("test-execute-");
		List<IdmIdentityDto> identities = identityService.find(identityFilter, new PageRequest(0, 20, new Sort(Direction.ASC, IdmIdentity_.firstName.getName()))).getContent();
	
		assertEquals(5, identities.size());
		for (int index = 0; index < 5; index++) {
			assertEquals(String.valueOf(index), identities.get(index).getFirstName());
		}
	}
	
	/**
	 * Method prepare test authorities for script.
	 * 
	 * @param scriptDto
	 */
	private void prepareAuthForTestScript(IdmScriptDto scriptDto) {
		IdmScriptAuthorityDto authDto = new IdmScriptAuthorityDto();
		authDto.setType(ScriptAuthorityType.CLASS_NAME);
		authDto.setClassName("eu.bcvsolutions.idm.core.scheduler.task.impl.ExecuteScriptTaskExecutor");
		authDto.setScript(scriptDto.getId());
		scriptAuthorityService.save(authDto);
	}
	
}
