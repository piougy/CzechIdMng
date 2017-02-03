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
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.TaskTriggerState;
import eu.bcvsolutions.idm.core.scheduler.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.service.api.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.service.api.SchedulerService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultLoginService;

/**
 * Default implementation of {@link SchedulerService}. 
 * This implementation adds long running task to queue and not execute them directly.
 * It is needed for choose server instance id, where task will be physically executed (more instances can read one database).
 * 
 * @author Radek Tomiška
 */
@Service
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class DefaultSchedulerService implements SchedulerService {

	/**
	 * Name of task group
	 */
	public static final String DEFAULT_GROUP_NAME = "default";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLoginService.class);
	private final ApplicationContext context;
	private final Scheduler scheduler;
	
	@Autowired
	public DefaultSchedulerService(
			ApplicationContext context,
			Scheduler scheduler) {
		Assert.notNull(context);
		Assert.notNull(scheduler);
		//
		this.context = context;
		this.scheduler = scheduler;
	}
	
	@Override
	public List<Task> getSupportedTasks() {		
		List<Task> tasks = new ArrayList<>();		
		for (Map.Entry<String, SchedulableTaskExecutor> entry : context.getBeansOfType(SchedulableTaskExecutor.class)
				.entrySet()) {
			SchedulableTaskExecutor taskExecutor = entry.getValue();
			Task task = new Task();
			task.setId(entry.getKey());
			task.setModule(taskExecutor.getModule());
			task.setTaskType(taskExecutor.getClass());
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
		} catch (SchedulerException ex) {
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
			Task task = new Task();
			// task setting
			task.setId(jobKey.getName());
			task.setTaskType((Class<? extends SchedulableTaskExecutor>) jobDetail.getJobClass());
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
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public Task createTask(Task task) {
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
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}

	@Override
	public void deleteTask(String taskId) {		
		try {
			scheduler.deleteJob(new JobKey(taskId, DEFAULT_GROUP_NAME));
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}

	@Override
	public AbstractTaskTrigger runTask(String taskId) {
		try {
			JobKey taskKey = getKey(taskId);
			for(JobExecutionContext jobContext : scheduler.getCurrentlyExecutingJobs()) {
				if (jobContext.getJobDetail().getKey().equals(taskKey)) {
					throw new CoreException("job is already running on this scheduler instance");
				}
			}
			// run job - add simple trigger
			SimpleTaskTrigger trigger = new SimpleTaskTrigger();
			trigger.setTaskId(taskId);
			trigger.setDescription("run manually");
			trigger.setFireTime(new DateTime());
			return createTrigger(taskId, trigger);
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public boolean interruptTask(String taskId) {
		try {
			return scheduler.interrupt(getKey(taskId));
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}

	@Override
	public AbstractTaskTrigger createTrigger(String taskId, AbstractTaskTrigger trigger) {
		try {
			String triggerId = Key.createUniqueName(taskId);
			trigger.setId(triggerId);

			if (trigger instanceof CronTaskTrigger) {
				scheduler.scheduleJob(
						TriggerBuilder.newTrigger()
							.withIdentity(triggerId, taskId)
							.forJob(getKey(taskId))
							.withDescription(trigger.getDescription())
							.withSchedule(CronScheduleBuilder
									.cronSchedule(((CronTaskTrigger) trigger).getCron())
									.withMisfireHandlingInstructionDoNothing()
									.inTimeZone(TimeZone.getDefault()))
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
				throw new CoreException("Unsupported type of task trigger " + trigger);
			}

			return trigger;
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}

	@Override
	public void deleteTrigger(String taskId, String triggerId) {
		try {
			scheduler.unscheduleJob(new TriggerKey(triggerId, taskId));
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}

	@Override
	public void pauseTrigger(String taskName, String triggerId) {
		try {
			scheduler.pauseTrigger(new TriggerKey(triggerId, taskName));
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
		}
	}

	@Override
	public void resumeTrigger(String taskName, String triggerId) {
		try {
			scheduler.resumeTrigger(new TriggerKey(triggerId, taskName));
		} catch (SchedulerException ex) {
			throw new CoreException(ex);
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
