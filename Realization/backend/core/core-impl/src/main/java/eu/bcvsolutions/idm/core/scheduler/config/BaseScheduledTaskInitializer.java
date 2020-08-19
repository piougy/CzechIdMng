package eu.bcvsolutions.idm.core.scheduler.config;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.TaskTriggerState;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.jaxb.IdmScheduledTaskParameterType;
import eu.bcvsolutions.idm.core.scheduler.jaxb.IdmScheduledTaskTriggerType;
import eu.bcvsolutions.idm.core.scheduler.jaxb.IdmScheduledTaskType;
import eu.bcvsolutions.idm.core.scheduler.jaxb.IdmScheduledTasksType;

/**
 * Init scheduled long running tasks from configuration file.
 * Usable in event processor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @since 10.5.0 
 */
public abstract class BaseScheduledTaskInitializer {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BaseScheduledTaskInitializer.class);
	protected static final String DEFAULT_RESOURCE = "eu/bcvsolutions/idm/tasks/";

	@Autowired private SchedulerManager schedulerService;

	protected JAXBContext jaxbContext = null;

	public BaseScheduledTaskInitializer() {
		// init jaxb
		try {
			jaxbContext = JAXBContext.newInstance(IdmScheduledTasksType.class);
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
	}

	/**
	 * Return resource with xml, that contains scheduled tasks.
	 * 
	 * @return
	 */
	protected abstract InputStream getTasksInputStream();

	/**
	 * Return module id from module descriptor.
	 * 
	 * @return
	 */
	protected String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	protected abstract String getTasksXmlPath();
	
	/**
	 * Init scheduled long running tasks for configuration file
	 * 
	 * @since 10.5.0
	 */
	public void init() {
		// init default long running task
		initScheduledTask(getTasksInputStream());
	}

	/**
	 * Method initial default long running tasks. Task will be loaded from
	 * resources.
	 * 
	 * @param tasksInputStream
	 */
	protected void initScheduledTask(InputStream tasksInputStream) {
		if (tasksInputStream == null) {
			LOG.warn("For module: [{}] is empty parameter 'tasksInputStream', skip init tasks for this module.",
					getModule());
		}
		Unmarshaller jaxbUnmarshaller = null;
		//
		try {
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}

		try {
			IdmScheduledTasksType tasks = (IdmScheduledTasksType) jaxbUnmarshaller.unmarshal(tasksInputStream);

			List<Task> allExistingTasks = schedulerService.getAllTasks();
			if (tasks != null) {
				for (IdmScheduledTaskType taskType : tasks.getTasks()) {
					try {
						Task task = typeToTask(taskType);
						LOG.debug("Task with type [{}] is successfully initialized. Try to save.",
								taskType.getTaskType());

						if (existSimilarTask(task, allExistingTasks)) {
							LOG.debug("Task with type [{}] already exist, skip save this task.",
									taskType.getTaskType());
							continue;
						}
						LOG.info("Create new task with type [{}].", taskType.getTaskType());
						task = schedulerService.createTask(task);
						createAndSaveTriggers(task, taskType);
					} catch (ClassNotFoundException e) {
						LOG.error(
								"Scheduled task with task type [{}] can't be init. Skip this task. Error message: [{}]",
								taskType.getTaskType(), e.getMessage());
					}
				}
			}
		} catch (JAXBException e) {
			LOG.error("Scheduled task type validation failed, file name: {}, module: {}, error message: {}",
					getTasksXmlPath(), getModule(), e.getLocalizedMessage());
		}
	}

	/**
	 * Transform {@link IdmScheduledTaskType} to {@link Task}.
	 * 
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Task typeToTask(IdmScheduledTaskType type) throws ClassNotFoundException {
		Task task = new Task();
		task.setDescription(type.getDescription());
		task.setModule(getModule()); // TODO: module attribute not working in Task
		task.setInstanceId(type.getInstanceId());

		@SuppressWarnings("unchecked")
		Class<? extends SchedulableTaskExecutor<?>> classType = (Class<? extends SchedulableTaskExecutor<?>>) Class
				.forName(type.getTaskType());
		task.setTaskType(classType);

		// parameters
		if (type.getParameters() != null && type.getParameters().getParameters() != null) {
			List<IdmScheduledTaskParameterType> taskParameters = type.getParameters().getParameters();
			Map<String, String> parameters = new HashMap<>(taskParameters.size());
			for (IdmScheduledTaskParameterType param : taskParameters) {
				parameters.put(param.getKey(), param.getValue());
			}
			if (!parameters.isEmpty()) {
				task.setParameters(parameters);
			}
		}

		return task;
	}

	/**
	 * Create and save trigger from {@link IdmScheduledTaskType}. 
	 * 
	 * @param task
	 * @param type
	 */
	private void createAndSaveTriggers(Task task, IdmScheduledTaskType type) {
		Assert.notNull(task, "Task is required.");
		Assert.notNull(task.getId(), "Task identifier is required.");
		// triggers
		if (type.getTriggers() != null && type.getTriggers().getTriggers() != null) {
			for (IdmScheduledTaskTriggerType trigger : type.getTriggers().getTriggers()) {
				AbstractTaskTrigger newTrigger = null;
				try {
					newTrigger = getTrigger(trigger);
				} catch (ClassNotFoundException e) {
					LOG.error(
							"Trigger type: [{}], not exist. Skip init this trigger for task id: [{}]. Error message: [{}]",
							trigger.getType(), task.getId(), e.getMessage());
					continue;
				}
				if (newTrigger != null) {
					LOG.info("Create new trigger for task id: [{}]. Trigger type: [{}].", task.getId(),
							trigger.getType());
					schedulerService.createTrigger(task.getId(), newTrigger);
				}
			}
		}
	}

	/**
	 * Method return {@link AbstractTaskTrigger} from
	 * {@link IdmScheduledTaskTriggerType}
	 * 
	 * @param triggerType
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected AbstractTaskTrigger getTrigger(IdmScheduledTaskTriggerType triggerType) throws ClassNotFoundException {

		@SuppressWarnings("unchecked")
		Class<AbstractTaskTrigger> triggerClass = (Class<AbstractTaskTrigger>) Class.forName(triggerType.getType());
		if (triggerClass.isAssignableFrom(SimpleTaskTrigger.class)) {
			SimpleTaskTrigger simpleTrigger = new SimpleTaskTrigger();
			simpleTrigger = (SimpleTaskTrigger) setTriggerDefaultValues(simpleTrigger, triggerType);
			simpleTrigger.setFireTime(Instant.ofEpochMilli(triggerType.getFireTime()).atZone(ZoneId.systemDefault()));
			return simpleTrigger;
		} else if (triggerClass.isAssignableFrom(CronTaskTrigger.class)) {
			CronTaskTrigger cronTrigger = new CronTaskTrigger();
			cronTrigger = (CronTaskTrigger) setTriggerDefaultValues(cronTrigger, triggerType);
			cronTrigger.setCron(triggerType.getCron());
			return cronTrigger;
		} else {
			LOG.error(
					"Trigger type: [{}] is not implemented, please override method getTrigger from AbstractScheduledTaskInitializer. "
							+ "This trigger will be skipped.",
					triggerType.getType());
			return null;
		}
	}

	/**
	 * Method set default variables from {@link AbstractTaskTrigger}
	 * 
	 * @param trigger
	 * @param triggerType
	 * @return
	 */
	private AbstractTaskTrigger setTriggerDefaultValues(AbstractTaskTrigger trigger,
			IdmScheduledTaskTriggerType triggerType) {
		trigger.setDescription(triggerType.getDescription());
		trigger.setNextFireTime(triggerType.getNextFireTime());
		trigger.setPreviousFireTime(triggerType.getPreviousFireTime());
		trigger.setState(TaskTriggerState.valueOf(triggerType.getState()));
		return trigger;
	}

	/**
	 * Method check if in parameter {@param allExistingTasks} exist task given
	 * in parameter {@param task}. Check for task type.
	 * 
	 * @param task
	 * @param allExistingTasks
	 * @return
	 */
	protected boolean existSimilarTask(Task task, List<Task> allExistingTasks) {
		for (Task allExistingTask : allExistingTasks) {
			// check if exist this type
			if (task.getTaskType().equals(allExistingTask.getTaskType())) {
				return true;
			}
		}
		return false;
	}
}
