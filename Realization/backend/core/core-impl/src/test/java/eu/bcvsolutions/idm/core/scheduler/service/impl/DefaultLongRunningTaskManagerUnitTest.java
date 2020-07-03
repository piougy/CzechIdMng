package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Long running tasks test
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultLongRunningTaskManagerUnitTest extends AbstractUnitTest {

	@Mock private IdmLongRunningTaskService service;
	@Mock private Executor executor;
	@Mock private ConfigurationService configurationService;
	@Mock private SecurityService securityService;
	@Mock private EntityEventManager entityEventManager;
	@Mock private AttachmentManager attachmentManager;
	//
	@InjectMocks
	private DefaultLongRunningTaskManager manager;
	
	@Test
	public void testSaveSyncTaskAsRunning() {
		TestSchedulableTask taskExecutor = new TestSchedulableTask() {
			@Override
			public String call() {
				return "mock";
			}
		};
		IdmLongRunningTaskDto mockTask = new IdmLongRunningTaskDto();
		mockTask.setInstanceId("instanceId");
		mockTask.setResult(new OperationResult(OperationState.RUNNING));
		Holder holder = new Holder(mockTask);
		Mockito.when(service.save(Mockito.any())).thenAnswer(new Answer<IdmLongRunningTaskDto>() {
		    @Override
		    public IdmLongRunningTaskDto answer(InvocationOnMock invocation) throws Throwable {
		      Object[] args = invocation.getArguments();
		      holder.value = (IdmLongRunningTaskDto) args[0];
		      return holder.value;
		    }
		  });
		Mockito.when(service.get(Mockito.any())).thenReturn(holder.value);
		Mockito.when(configurationService.getInstanceId()).thenReturn("instanceId");
		//
		manager.executeSync(taskExecutor);
		//
		// running - has to be save just one
		ArgumentCaptor<IdmLongRunningTaskDto> argument = ArgumentCaptor.forClass(IdmLongRunningTaskDto.class);
		Mockito.verify(service, Mockito.times(1)).save(argument.capture());
		Assert.assertEquals(OperationState.RUNNING, argument.getValue().getResultState());		
	}
	
	@Test
	public void testFailedLoggedTask() {
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto(UUID.randomUUID());
		task.setResult(new OperationResult(OperationState.CREATED));
		task.setTaskType(TestSchedulableTask.class.getCanonicalName());
		task.setInstanceId("mock");
		List<IdmLongRunningTaskDto> tasks = Lists.newArrayList(task);
		//
		Mockito.when(configurationService.getInstanceId()).thenReturn(task.getInstanceId());
		Mockito.when(service.findAllByInstance(Mockito.any(), Mockito.any())).thenReturn(tasks);
		Mockito.when(service.get(Mockito.any())).thenReturn(task);
		//
		manager.processCreated();
		manager.processCreated();
		manager.processCreated();
		//
		Assert.assertEquals(1, manager.getFailedLoggedTask().size());
		Assert.assertTrue(manager.getFailedLoggedTask().contains(task.getId()));
	}
	
	private static class Holder {
		
		IdmLongRunningTaskDto value;
		
		public Holder(IdmLongRunningTaskDto value) {
			this.value = value;
		}
	}
}
