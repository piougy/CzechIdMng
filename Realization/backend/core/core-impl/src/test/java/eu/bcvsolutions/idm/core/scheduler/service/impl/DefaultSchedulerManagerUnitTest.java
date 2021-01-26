package eu.bcvsolutions.idm.core.scheduler.service.impl;

import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.apache.camel.spring.GenericBeansException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.Calendar;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmDependentTaskTriggerRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Scheduler unit test to cover error states.
 * 
 * TODO: cover all exceptions
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSchedulerManagerUnitTest extends AbstractUnitTest {
	
	@Mock private AutowireCapableBeanFactory contextFactory;
	@Mock private ApplicationContext context;
	@Mock private Scheduler scheduler;
	@Mock private IdmDependentTaskTriggerRepository dependentTaskTriggerRepository;
	//
	@InjectMocks 
	private DefaultSchedulerManager manager;
	
	@Test(expected = CoreException.class)
	public void testExceptionOnGetAllTasks() throws SchedulerException {
		when(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(DefaultSchedulerManager.DEFAULT_GROUP_NAME)))
			.thenThrow(new org.quartz.SchedulerException());
		//
		manager.getAllTasks();
	}
	
	public void testNullTaskOnGetAllTasks() {
		// TODO ...
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testInvalidTriggerType() throws SchedulerException {
		JobDetail jobDetail = JobBuilder.newJob()
				.withIdentity("test", DefaultSchedulerManager.DEFAULT_GROUP_NAME)
				.withDescription("desc")
				.ofType(TestSchedulableTask.class)
				.usingJobData(new JobDataMap())
				.storeDurably()
				.build();
		//
		Trigger trigger = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInHours(1).build();
		//
		when(scheduler.getJobDetail(jobDetail.getKey())).thenReturn(jobDetail);
		when(scheduler.getTriggersOfJob(jobDetail.getKey())).thenReturn((List) Lists.newArrayList(trigger));
		when(context.getAutowireCapableBeanFactory()).thenReturn(contextFactory);
		when(contextFactory.createBean(Mockito.any())).thenReturn(new TestSchedulableTask());
		//
		Task task = manager.getTask("test");
		//
		Assert.assertTrue(task.getTriggers().isEmpty());
		Assert.assertTrue(task.isTrimmed());
	}

	@Test(expected = CoreException.class)
	public void testSchedulerExceptionOnGetTask() throws SchedulerException {
		when(scheduler.getJobDetail(Mockito.any())).thenThrow(new org.quartz.SchedulerException());
		//
		 manager.getTask("test");
	}
	
	@Test
	public void testClassNotFoundExceptionOnGetTask() throws SchedulerException {
		when(scheduler.getJobDetail(Mockito.any())).thenThrow(new org.quartz.SchedulerException(new ClassNotFoundException()));
		//
		Assert.assertNull(manager.getTask("test"));
	}
	
	@Test
	public void testBeansExceptionOnGetTask() throws SchedulerException {
		when(scheduler.getJobDetail(Mockito.any())).thenThrow(new GenericBeansException("mock"));
		//
		Assert.assertNull(manager.getTask("test"));
	}
	
	@Test
	public void testIllegalArgumentExceptionOnGetTask() throws SchedulerException {
		when(scheduler.getJobDetail(Mockito.any())).thenThrow(new IllegalArgumentException("mock"));
		//
		Assert.assertNull(manager.getTask("test"));
	}
	
	@Test
	public void testNotRunTaskWithExecuteDate() throws JobExecutionException {
	    TestSchedulableTask mockTask = new TestSchedulableTask();
	    JobExecutionContext context = new JobExecutionContext() {
			
			@Override
			public void setResult(Object result) {				
			}
			
			@Override
			public void put(Object key, Object value) {
				
			}
			
			@Override
			public boolean isRecovering() {
				return false;
			}
			
			@Override
			public Trigger getTrigger() {
				return null;
			}
			
			@Override
			public Scheduler getScheduler() {
				return null;
			}
			
			@Override
			public Date getScheduledFireTime() {
				return null;
			}
			
			@Override
			public Object getResult() {
				return null;
			}
			
			@Override
			public int getRefireCount() {
				return 0;
			}
			
			@Override
			public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
				return null;
			}
			
			@Override
			public Date getPreviousFireTime() {
				return null;
			}
			
			@Override
			public Date getNextFireTime() {
				return null;
			}
			
			@Override
			public JobDataMap getMergedJobDataMap() {
				JobDataMap map = new JobDataMap();
				map.put(EntityEvent.EVENT_PROPERTY_EXECUTE_DATE, ZonedDateTime.now().plusHours(1).toString());
				//
				return map;
			}
			
			@Override
			public long getJobRunTime() {
				return 0;
			}
			
			@Override
			public Job getJobInstance() {
				return null;
			}
			
			@Override
			public JobDetail getJobDetail() {
				return null;
			}
			
			@Override
			public Date getFireTime() {
				return null;
			}
			
			@Override
			public String getFireInstanceId() {
				return null;
			}
			
			@Override
			public Calendar getCalendar() {
				return null;
			}
			
			@Override
			public Object get(Object key) {
				return null;
			}
		};
		// mock task ends silently - execute time is in future
		// exception - failure
		mockTask.execute(context);
	}
}
