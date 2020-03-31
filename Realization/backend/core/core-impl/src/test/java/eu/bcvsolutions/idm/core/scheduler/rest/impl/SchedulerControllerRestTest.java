package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.TestRegistrableSchedulableTask;
import eu.bcvsolutions.idm.core.scheduler.service.impl.TestSchedulableTask;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Scheduler tests.
 * - filter
 * 
 * @author Radek Tomiška
 *
 */
public class SchedulerControllerRestTest extends AbstractRestTest {
	
	@Autowired private SchedulerManager manager;
	
	@Test
	public void testFindByText() {
		// create two tasks
		Task taskOne = createTask(TestSchedulableTask.class, "mock" + getHelper().createName());
		Task taskTwo = createTask(TestRegistrableSchedulableTask.class, "mock" + getHelper().createName());
		
		TaskFilter filter = new TaskFilter();
		filter.setText("SchedulableTask");
		List<Task> results = find(filter);
		//
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskTwo.getId())));
		//
		filter.setText("mock");
		results = find(filter);
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskTwo.getId())));
		//
		filter.setText(TestRegistrableSchedulableTask.class.getSimpleName());
		results = find(filter);
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().allMatch(t -> !t.getId().equals(taskOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(t -> t.getId().equals(taskTwo.getId())));
		//
		filter.setText(taskOne.getDescription());
		results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().allMatch(t -> t.getId().equals(taskOne.getId())));
	}
	
	protected List<Task> find(TaskFilter filter) {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/scheduler-tasks")
	        		.with(authentication(getAdminAuthentication()))
	        		.params(toQueryParams(filter))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, Task.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	private Task createTask(Class<? extends SchedulableTaskExecutor<?>> taskType, String description) {
		Task task = new Task();
		task.setInstanceId("mock");
		task.setTaskType(taskType);
		task.setDescription(description);
		//
		return manager.createTask(task);
	}
}
