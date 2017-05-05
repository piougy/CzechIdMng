package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.TaskTriggerState;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.exception.InvalidCronExpressionException;
import eu.bcvsolutions.idm.core.scheduler.exception.SchedulerException;

/**
 * Default implementation of {@link SchedulerManager}. 
 * This implementation adds long running task to queue and not execute them directly.
 * It is needed for choose server instance id, where task will be physically executed (more instances can read one database).
 * 
 * @author Radek Tomiška
 */
public class DefaultSchedulerManager implements SchedulerManager {

	/**
	 * Name of task group
	 */
	public static final String DEFAULT_GROUP_NAME = "default";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSchedulerManager.class);
	private final ApplicationContext context;
	private final Scheduler scheduler;
	
	public DefaultSchedulerManager(
			ApplicationContext context,
			Scheduler scheduler) {
		Assert.notNull(context);
		Assert.notNull(scheduler);
		//
		this.context = context;
		this.scheduler = scheduler;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Task> getSupportedTasks() {		
		List<Task> tasks = new ArrayList<>();		
		for (Map.Entry<String, SchedulableTaskExecutor> entry : context.getBeansOfType(SchedulableTaskExecutor.class)
				.entrySet()) {
			SchedulableTaskExecutor<?> taskExecutor = entry.getValue();
			Task task = new Task();
			task.setId(entry.getKey());
			task.setModule(taskExecutor.getModule());
			task.setTaskType((Class<? extends SchedulableTaskExecutor<?>>) taskExecutor.getClass());
			task.setDescription(AutowireHelper.getBeanDescription(entry.getKey()));
			for (String parameterName : taskExecutor.getParameterNames()) {
				task.getParameters().put(parameterName, null);
			}
			tasks.add(task);
		}
		return tasks;
	}

	@Override
	public List<Task> getAllTasks() {
		try {
			List<Task> tasks = new ArrayList<>();
			
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(DEFAULT_GROUP_NAME))) {
				tasks.add(getTask(jobKey));
			}

			return tasks;
		} catch (org.quartz.SchedulerException ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public Task getTask(String taskId) {
		return getTask(getKey(taskId));
	}
	
	/**
	 * Returns task by given key
	 * 
	 * @param jobKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Task getTask(JobKey jobKey) {
		try {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			if (jobDetail == null) {
				// job does not exists
				return null;
			}
			Task task = new Task();
			// task setting
			task.setId(jobKey.getName());
			task.setTaskType((Class<? extends SchedulableTaskExecutor<?>>) jobDetail.getJobClass());
			task.setDescription(jobDetail.getDescription());
			task.setInstanceId(jobDetail.getJobDataMap().getString(SchedulableTaskExecutor.PARAMETER_INSTANCE_ID));
			task.setTriggers(new ArrayList<>());
			// task properties
			for(Entry<String, Object> entry : jobDetail.getJobDataMap().entrySet()) {
				task.getParameters().put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
			}
			// scheduled triggers
			for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
				TriggerState state = scheduler.getTriggerState(trigger.getKey());	
				if (trigger instanceof CronTrigger) {
					task.getTriggers().add(new CronTaskTrigger(task.getId(), (CronTrigger) trigger, TaskTriggerState.convert(state)));
				} else if (trigger instanceof SimpleTrigger) {
					task.getTriggers().add(new SimpleTaskTrigger(task.getId(), (SimpleTrigger) trigger, TaskTriggerState.convert(state)));
				} else {
					LOG.warn("Job '{}' ({}) has registered trigger of unsupported type {}", jobKey,
							jobDetail.getJobClass(), trigger);
				}
			}
			return task;
		} catch (org.quartz.SchedulerException ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public Task createTask(Task task) {
		Assert.notNull(task);
		Assert.notNull(task.getInstanceId());
		Assert.notNull(task.getTaskType());
		//
		try {
			// task id
			String taskId = Key.createUniqueName(DEFAULT_GROUP_NAME);
			// description
			String description = task.getDescription();
			if (StringUtils.isEmpty(description)) {
				description = AutowireHelper.getBeanDescription(task.getTaskType());
			}
			// job properties
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put(SchedulableTaskExecutor.PARAMETER_INSTANCE_ID, task.getInstanceId());
			task.getParameters().entrySet().forEach(entry -> {
				jobDataMap.put(entry.getKey(), entry.getValue());
			});
			// validate init method
			try {
				LongRunningTaskExecutor<?> taskExecutor = AutowireHelper.createBean(task.getTaskType());
				taskExecutor.init(jobDataMap);
			} catch (ResultCodeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_INIT_FAILED, 
						ImmutableMap.of("taskId", taskId, "taskType", task.getTaskType(), "instanceId", task.getInstanceId()), ex);
			}
			// create job detail - job definition
			JobDetail jobDetail = JobBuilder.newJob()
					.withIdentity(taskId, DEFAULT_GROUP_NAME)
					.withDescription(description)
					.ofType(task.getTaskType())
					.usingJobData(jobDataMap)
					.storeDurably()
					.build();
			// add job 
			scheduler.addJob(jobDetail, false);			
			//
			LOG.debug("Job '{}' ({}) was created and registered", taskId, task.getTaskType());
			//
			return getTask(taskId);
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_CREATE_TASK_FAILED, ex);
		}
	}

	@Override
	public void deleteTask(String taskId) {		
		try {
			scheduler.deleteJob(new JobKey(taskId, DEFAULT_GROUP_NAME));
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_DELETE_TASK_FAILED, ex);
		}
	}

	@Override
	public AbstractTaskTrigger runTask(String taskId) {
		// run job - add simple trigger
		SimpleTaskTrigger trigger = new SimpleTaskTrigger();
		trigger.setTaskId(taskId);
		trigger.setDescription("run manually");
		trigger.setFireTime(new DateTime());
		return createTrigger(taskId, trigger);
	}
	
	@Override
	public boolean interruptTask(String taskId) {
		try {
			return scheduler.interrupt(getKey(taskId));
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_INTERRUPT_TASK_FAILED, ex);
		}
	}

	@Override
	public AbstractTaskTrigger createTrigger(String taskId, AbstractTaskTrigger trigger) {
		Assert.notNull(taskId);
		Assert.notNull(trigger);
		//
		try {
			String triggerId = Key.createUniqueName(taskId);
			trigger.setId(triggerId);
			trigger.setTaskId(taskId);
			//
			if (trigger instanceof CronTaskTrigger) {
				CronTaskTrigger cronTaskTrigger = (CronTaskTrigger) trigger;
				CronScheduleBuilder cronBuilder;
				try {
					cronBuilder = CronScheduleBuilder
							.cronSchedule(cronTaskTrigger.getCron())
							.withMisfireHandlingInstructionDoNothing()
							.inTimeZone(TimeZone.getDefault());
				} catch(RuntimeException ex) {
					throw new InvalidCronExpressionException(cronTaskTrigger.getCron(), ex);
				}
				//
				scheduler.scheduleJob(
						TriggerBuilder.newTrigger()
							.withIdentity(triggerId, taskId)
							.forJob(getKey(taskId))
							.withDescription(cronTaskTrigger.getDescription())
							.withSchedule(cronBuilder)
							.startNow()
							.build());
			} else if (trigger instanceof SimpleTaskTrigger) {
				scheduler.scheduleJob(
						TriggerBuilder.newTrigger()
							.withIdentity(triggerId, taskId)
							.forJob(getKey(taskId))
							.withDescription(trigger.getDescription())
							.withSchedule(SimpleScheduleBuilder.simpleSchedule())
							.startAt(((SimpleTaskTrigger) trigger).getFireTime().toDate())
							.build());
			} else {
				throw new SchedulerException(CoreResultCode.SCHEDULER_UNSUPPORTED_TASK_TRIGGER, ImmutableMap.of("trigger", trigger.getClass()));
			}

			return trigger;
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_CREATE_TRIGGER_FAILED, ex);
		}
	}

	@Override
	public void deleteTrigger(String taskId, String triggerId) {
		try {
			scheduler.unscheduleJob(new TriggerKey(triggerId, taskId));
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_DELETE_TRIGGER_FAILED, ex);
		}
	}

	@Override
	public void pauseTrigger(String taskId, String triggerId) {
		try {
			scheduler.pauseTrigger(new TriggerKey(triggerId, taskId));
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_PAUSE_TRIGGER_FAILED, ex);
		}
	}

	@Override
	public void resumeTrigger(String taskId, String triggerId) {
		try {
			scheduler.resumeTrigger(new TriggerKey(triggerId, taskId));
		} catch (org.quartz.SchedulerException ex) {
			throw new SchedulerException(CoreResultCode.SCHEDULER_RESUME_TRIGGER_FAILED, ex);
		}
	}
	
	/**
	 * Returns job key to given taskId
	 * 
	 * @param taskId
	 * @return
	 */
	private JobKey getKey(String taskId) {
		return new JobKey(taskId, DEFAULT_GROUP_NAME);
	}
}
