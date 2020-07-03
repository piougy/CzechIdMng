package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.ecm.entity.IdmAttachment;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.exception.ConcurrentExecutionException;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.exception.TaskNotRecoverableException;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Default implementation {@link LongRunningTaskManager}
 *
 * @author Radek Tomiška
 *
 */
public class DefaultLongRunningTaskManager implements LongRunningTaskManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLongRunningTaskManager.class);
	private final IdmLongRunningTaskService service;
	private final Executor executor;
	private final ConfigurationService configurationService;
	private final SecurityService securityService;
	private final EntityEventManager entityEventManager;
	private final Set<UUID> failedLoggedTask = new HashSet<>();
	//
	@Autowired private AttachmentManager attachmentManager;

	@Autowired
	public DefaultLongRunningTaskManager(
			IdmLongRunningTaskService service,
			@Qualifier(SchedulerConfiguration.TASK_EXECUTOR_NAME)
			Executor executor,
			EntityEventManager entityEventManager,
			ConfigurationService configurationService,
			SecurityService securityService) {
		Assert.notNull(service, "LRT service is required.");
		Assert.notNull(executor, "Thread executor is required.");
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(configurationService, "Service is required.");
		Assert.notNull(securityService, "Service is required.");
		//
		this.service = service;
		this.executor = executor;
		this.entityEventManager = entityEventManager;
		this.configurationService = configurationService;
		this.securityService = securityService;
	}

	/**
	 * Cancel all previously ran tasks
	 */
	@Override
	@Transactional
	public void init() {
		LOG.info("Cancel unprocessed long running task - tasks was interrupt during instance restart");
		//
		// task prepared for run - they can be in running state, but process physically doesn't started yet (running flag is still set to false)
		String instanceId = configurationService.getInstanceId();
		service
			.findAllByInstance(instanceId, OperationState.RUNNING)
			.forEach(this::cancelTaskByRestart);
		//
		// running tasks - they can be marked as canceled, but they were not killed before server was restarted
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setInstanceId(instanceId);
		filter.setRunning(Boolean.TRUE);
		service
			.find(filter, null)
			.forEach(this::cancelTaskByRestart);
	}

	/**
	 * Schedule {@link #processCreated()} only
	 */
	@Transactional
	@Scheduled(fixedDelayString = "${" + SchedulerConfiguration.PROPERTY_TASK_QUEUE_PROCESS + ":" + SchedulerConfiguration.DEFAULT_TASK_QUEUE_PROCESS + "}")
	public void scheduleProcessCreated() {
		if (!isAsynchronous()) {
			// asynchronous processing is disabled
			// prevent to debug some messages into log - usable for devs
			return;
		}
		processCreated();
	}

	/**
	 * Executes long running task on this instance
	 */
	@Override
	@Transactional
	public List<LongRunningFutureTask<?>> processCreated() {
		String instanceId = configurationService.getInstanceId();
		LOG.debug("Processing created tasks from long running task queue on instance id [{}]", instanceId);
		// run as system - called from scheduler internally
		securityService.setSystemAuthentication();
		//
		Set<String> processedTaskTypes = Sets.newHashSet();
		List<LongRunningFutureTask<?>> taskList = new ArrayList<LongRunningFutureTask<?>>();
		service.findAllByInstance(instanceId, OperationState.CREATED).forEach(task -> {
			String taskType = task.getTaskType();
			UUID taskId = task.getId();
			if (!processedTaskTypes.contains(taskType)) {
				try {
					LongRunningFutureTask<?> futureTask = processCreated(taskId);
					if (futureTask != null) {
						taskList.add(futureTask);
						// prevent to persisted task starts twice
						if (AutowireHelper.getTargetClass(futureTask.getExecutor()).isAnnotationPresent(DisallowConcurrentExecution.class)) {
							processedTaskTypes.add(taskType);
						}
						failedLoggedTask.remove(taskId);
					}
				} catch (ResultCodeException ex) {
					// we want to process other task, if some task fails and log just once
					processedTaskTypes.add(taskType);
					if (!failedLoggedTask.contains(taskId)) {
						// we want to know in log, some scheduled task is not complete before next execution attempt
						ExceptionUtils.log(LOG, ex);
						failedLoggedTask.add(taskId);
					}
				}
			}
		});
		return taskList;
	}

	@Override
	@Transactional
	public LongRunningFutureTask<?> processCreated(UUID longRunningTaskId) {
		LOG.debug("Processing created task [{}] from long running task queue", longRunningTaskId);
		//
		IdmLongRunningTaskDto task = service.get(longRunningTaskId);
		if (task == null) {
			throw new EntityNotFoundException(IdmLongRunningTask.class, longRunningTaskId);
		}
		// task cannot be started twice
		if (task.isRunning() || OperationState.RUNNING == task.getResultState()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_RUNNING, ImmutableMap.of("taskId", task.getId()));
		}
		if (OperationState.CREATED != task.getResultState()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_PROCESSED, ImmutableMap.of("taskId", task.getId()));
		}
		//
		LongRunningTaskExecutor<?> taskExecutor = createTaskExecutor(task);
		if (taskExecutor == null) {
			return null;
		}
		return execute(taskExecutor);
	}
	
	@Override
	@Transactional
	public LongRunningFutureTask<?> recover(UUID longRunningTaskId) {
		LOG.info("Processing task [{}] again", longRunningTaskId);
		//
		IdmLongRunningTaskDto task = service.get(longRunningTaskId);
		if (task == null) {
			throw new EntityNotFoundException(IdmLongRunningTask.class, longRunningTaskId);
		}
		if (task.isRunning() || OperationState.RUNNING == task.getResultState()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_RUNNING, ImmutableMap.of("taskId", task.getId()));
		}
		if (!task.isRecoverable()) {
			throw new TaskNotRecoverableException(CoreResultCode.LONG_RUNNING_TASK_NOT_RECOVERABLE, task);
		}
		//
		// clean previous state and create new LRT instance
		DtoUtils.clearAuditFields(task);
		task.setId(null);
		task.clearState();
		task.setResult(new OperationResult(OperationState.RUNNING)); // prevent to execute created task redundantly by asynchronous job
		task = service.save(task); // persist new task
		//
		LongRunningTaskExecutor<?> taskExecutor = createTaskExecutor(task);
		if (taskExecutor == null) {
			return null;
		}
		return execute(taskExecutor);
	}

	@Override
	@Transactional
	public synchronized <V> LongRunningFutureTask<V> execute(LongRunningTaskExecutor<V> taskExecutor) {
		if (!isAsynchronous()) {
			V result = executeSync(taskExecutor);
			// construct simple "sync" task
			return new LongRunningFutureTask<>(taskExecutor, new FutureTask<V>(() -> { return result; } ) {
				@Override
				public V get() {
					return result;
				}
			});
		}
		//
		// autowire task properties
		AutowireHelper.autowire(taskExecutor);
		// persist LRT as running => prevent to scheduler process the created tasks
		IdmLongRunningTaskDto persistTask = resolveLongRunningTask(taskExecutor, null, OperationState.RUNNING);
		//
		try {
			taskExecutor.validate(resolveLongRunningTask(taskExecutor, null, OperationState.RUNNING));
		} catch (ConcurrentExecutionException ex) {
			// task can be executed later, e.g. after previous task ends
			markTaskAsCreated(persistTask);
			//
			throw ex;
		}
		//
		LongRunningFutureTask<V> longRunnigFutureTask = new LongRunningFutureTask<>(taskExecutor, new FutureTask<>(taskExecutor));
		// execute - after original transaction is commited
		entityEventManager.publishEvent(longRunnigFutureTask);
		//
		return longRunnigFutureTask;
	}

	/**
	 * Executes given initialized task asynchronously.
	 * We need to wait to transaction commit, when asynchronous task is executed - data is prepared in previous transaction mainly
	 *
	 * @param futureTask
	 */
	@Transactional
	@TransactionalEventListener
	public synchronized<V> void executeInternal(LongRunningFutureTask<V> futureTask) {
		Assert.notNull(futureTask, "Future task is required.");
		LongRunningTaskExecutor<V> taskExecutor = futureTask.getExecutor();
		Assert.notNull(taskExecutor, "Task executor is required.");
		Assert.notNull(futureTask.getFutureTask(), "Future task wrapper is required.");
		//
		if (securityService.getUsername().contentEquals(SecurityService.SYSTEM_NAME)) {
			// each LRT executed by system (~scheduler) will have new transaction context
			TransactionContextHolder.setContext(TransactionContextHolder.createEmptyContext());
		}
		//
		markTaskAsRunning(getValidTask(taskExecutor));
		UUID longRunningTaskId = taskExecutor.getLongRunningTaskId();
		//
		LOG.debug("Execute task [{}] asynchronously, logged user [{}], transaction: [{}].",
				longRunningTaskId, securityService.getUsername(), TransactionContextHolder.getContext().getTransactionId());
		try {
			executor.execute(futureTask.getFutureTask());
		} catch (RejectedExecutionException ex) {
			// thread pool queue is full - wait for another try
			UUID taskId = futureTask.getExecutor().getLongRunningTaskId();
			LOG.warn("Execute task [{}] asynchronously will be postponed, all threads are in use.", taskId);
			//
			IdmLongRunningTaskDto task = service.get(taskId);
			markTaskAsCreated(task);
			//
			// Throw exception here doesn't make sense here - is after transaction listener => exception is not propagated to caller. 
		}
	}

	@Override
	@Transactional
	public <V> V executeSync(LongRunningTaskExecutor<V> taskExecutor) {
		// autowire task properties
		AutowireHelper.autowire(taskExecutor);
		// persist LRT - set state to running - prevent to execute task twice asynchronously by processCreated
		IdmLongRunningTaskDto task = resolveLongRunningTask(taskExecutor, null, OperationState.RUNNING);
		//
		try {
			task = getValidTask(taskExecutor);
		} catch (Exception ex) {
			// task can be executed later, e.g. after previous task ends
			markTaskAsCreated(task);
			//
			throw ex;
		}
		//
		LOG.debug("Execute task [{}] synchronously", task.getId());
		// execute
		try {
			return taskExecutor.call();
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_FAILED,
					ImmutableMap.of(
							"taskId", task.getId(),
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId()), ex);
		} finally {
			LOG.debug("Executing task [{}] synchronously ended", task.getId());
		}
	}

	@Override
	@Transactional
	public void cancel(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId, "Task identifier is required.");
		IdmLongRunningTaskDto task = service.get(longRunningTaskId);
		Assert.notNull(task, "Task is required.");
		//
		if (!OperationState.isRunnable(task.getResult().getState())) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_NOT_RUNNING,
					ImmutableMap.of(
							"taskId", longRunningTaskId,
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId())
					);
		}
		//
		task.setResult(new OperationResult.Builder(OperationState.CANCELED).build());
		task.setRunning(false);
		LOG.info("Long running task with id: [{}] was canceled.", task.getId());
		// running to false will be set by task himself
		service.save(task);
	}

	@Override
	@Transactional
	public boolean interrupt(UUID longRunningTaskId) {
		Assert.notNull(longRunningTaskId, "Task identifier is required.");
		IdmLongRunningTaskDto task = service.get(longRunningTaskId);
		Assert.notNull(longRunningTaskId, "Task identifier is required.");
		String instanceId = configurationService.getInstanceId();
		if (!task.getInstanceId().equals(instanceId)) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_DIFFERENT_INSTANCE,
					ImmutableMap.of(
							"taskId", longRunningTaskId,
							"taskInstanceId", task.getInstanceId(),
							"currentInstanceId", instanceId));
		}
		if (OperationState.RUNNING != task.getResult().getState()) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_NOT_RUNNING,
					ImmutableMap.of(
							"taskId", longRunningTaskId,
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId()));
		}
		//
		// interrupt thread
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			if (thread.getId() == task.getThreadId()) {
				ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_INTERRUPT,
						ImmutableMap.of(
								"taskId", task.getId(),
								"taskType", task.getTaskType(),
								"instanceId", task.getInstanceId()));
				Exception ex = null;
				try {
					thread.interrupt();
				} catch(Exception e) {
					ex = e;
					LOG.error(resultModel.toString(), e);
				}
				task.setRunning(false);
				//
				if (ex == null) {
					LOG.info("Long running task with id: [{}], was interrupted.", task.getId());
					task.setResult(new OperationResult.Builder(OperationState.CANCELED).setModel(resultModel).build());
				} else {
					LOG.info("Long running task with id: [{}], has some exception during interrupt.", task.getId());
					task.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
				}
				service.save(task);
				return true;
			}
		}
		LOG.warn("For long running task with id: [{}], has not found running thread.", task.getId());
		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmLongRunningTaskDto getLongRunningTask(UUID longRunningTaskId, BasePermission... permission) {
		Assert.notNull(longRunningTaskId, "Long running task id is required. Long running task has to be executed at first.");
		//
		IdmLongRunningTaskFilter context = new IdmLongRunningTaskFilter();
		context.setIncludeItemCounts(true);
		//
		return service.get(longRunningTaskId, context, permission);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmLongRunningTaskDto getLongRunningTask(LongRunningTaskExecutor<?> taskExecutor, BasePermission... permission) {
		Assert.notNull(taskExecutor, "Long running task execturor is required.");
		Assert.notNull(taskExecutor.getLongRunningTaskId(), "Long running task id is required. Long running task has to be executed at first.");
		//
		return getLongRunningTask(taskExecutor.getLongRunningTaskId());
	}

	@Override
	@Transactional(readOnly = true)
	public IdmLongRunningTaskDto getLongRunningTask(LongRunningFutureTask<?> futureTask, BasePermission... permission) {
		Assert.notNull(futureTask, "Long running future task is required.");
		//
		return getLongRunningTask(futureTask.getExecutor(), permission);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmLongRunningTaskDto> findLongRunningTasks(IdmLongRunningTaskFilter filter, Pageable pageable, BasePermission... permission) {
		return service.find(filter, pageable, permission);
	}

	@Override
	public boolean isAsynchronous() {
		return configurationService.getBooleanValue(
				SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED,
				SchedulerConfiguration.DEFAULT_TASK_ASYNCHRONOUS_ENABLED);
	}

	@Override
	public IdmAttachmentDto getAttachment(UUID longRunningTaskId, UUID attachmentId, BasePermission... permission) {
		Assert.notNull(longRunningTaskId, "Task identifier is required.");
		Assert.notNull(attachmentId, "Attachment identifier is required");

		IdmLongRunningTaskDto longRunningTaskDto = service.get(longRunningTaskId, permission);

		if (longRunningTaskDto == null) {
			throw new EntityNotFoundException(service.getEntityClass(), longRunningTaskId);
		}

		IdmAttachmentDto attachmentDto = attachmentManager.get(attachmentId, permission);

		if (attachmentDto == null) {
			throw new EntityNotFoundException(IdmAttachment.class, attachmentId);
		}

		if (!ObjectUtils.isEmpty(PermissionUtils.trimNull(permission)) && !attachmentDto.getOwnerId().equals(longRunningTaskDto.getId())) {
			throw new ForbiddenEntityException((Serializable)attachmentId, PermissionUtils.trimNull(permission));
		}
		//
		return attachmentDto;
	}

	@Override
	@Transactional
	public IdmLongRunningTaskDto resolveLongRunningTask(LongRunningTaskExecutor<?> taskExecutor, UUID sheduledTaskId, OperationState state) {
		Assert.notNull(taskExecutor, "Task executor is required.");
		if (state == null) {
			// default
			state = OperationState.CREATED;
		}
		//
		// prepare task
		IdmLongRunningTaskDto task;
		if (taskExecutor.getLongRunningTaskId() == null) {
			task = new IdmLongRunningTaskDto();
			task.setTaskType(AutowireHelper.getTargetType(taskExecutor));
			task.setTaskProperties(taskExecutor.getProperties());
			task.setTaskDescription(taskExecutor.getDescription());
			task.setScheduledTask(sheduledTaskId);
			task.setInstanceId(configurationService.getInstanceId());
			task.setRecoverable(taskExecutor.isRecoverable());
			task.setResult(new OperationResult.Builder(state).build());
			// each LRT executed from the queue will have new transaction context
			if (state == OperationState.CREATED) {
				task.getTaskProperties().put(
						LongRunningTaskExecutor.PARAMETER_TRANSACTION_CONTEXT,
						TransactionContextHolder.createEmptyContext()
						);
			}
			// LRT is saved in new transaction implicitly.
			task = service.save(task);
			taskExecutor.setLongRunningTaskId(task.getId());
		} else {
			// load
			task = service.get(taskExecutor.getLongRunningTaskId());
		}
		return task;
	}
	
	/**
	 * Returns failed task identifiers.
	 * 
	 * @return failed task identifiers
	 * @since 10.4.0
	 */
	protected Set<UUID> getFailedLoggedTask() {
		return failedLoggedTask;
	}

	/**
	 * TODO: RUNNING state and running flag can look as redundant, but is not now - we need to flag them
	 * from scheduler side (prevent to try execute prepared LRT twice) and thread side (physical run).
	 * COS: more LRT than physical running are shown in agenda (depends on thread count configuration).
	 * Rewrite LRT engine - use some library for batch processing (e.g. Spring batch).
	 *
	 * @param task
	 * @return
	 */
	private synchronized IdmLongRunningTaskDto markTaskAsRunning(IdmLongRunningTaskDto task) {
		task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		// LRT is saved in new transaction implicitly.
		return service.save(task);
	}

	/**
	 * Task was rejected from thread poll (exhausted) - return task to prepared state.
	 *
	 * @param task
	 * @return
	 */
	private synchronized IdmLongRunningTaskDto markTaskAsCreated(IdmLongRunningTaskDto task) {
		task.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		// LRT is saved in new transaction implicitly.
		return service.save(task);
	}

	/**
	 * Returns executor's LRT task, when task is valid. Throws exception otherwise.
	 *
	 * @param taskExecutor
	 * @return
	 */
	private IdmLongRunningTaskDto getValidTask(LongRunningTaskExecutor<?> taskExecutor) {
		IdmLongRunningTaskDto task = service.get(taskExecutor.getLongRunningTaskId());
		Assert.notNull(task, "Task is required.");
		//
		if (!task.getInstanceId().equals(configurationService.getInstanceId())) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_DIFFERENT_INSTANCE,
					ImmutableMap.of("taskId", task.getId(), "taskInstanceId", task.getInstanceId(), "currentInstanceId", configurationService.getInstanceId()));
		}
		taskExecutor.validate(task);
		return task;
	}

	/**
	 * Create new LongRunningTaskExecutor from given LRT.
	 * Handles exceptions, when task already processed, task type is removed or task initialization failed
	 *
	 * @param task
	 * @return
	 */
	private LongRunningTaskExecutor<?> createTaskExecutor(IdmLongRunningTaskDto task) {
		Assert.notNull(task, "Long running task instance is required!");
		if (!OperationState.isRunnable(task.getResultState())) {
			throw new ResultCodeException(CoreResultCode.LONG_RUNNING_TASK_IS_PROCESSED, ImmutableMap.of("taskId", task.getId()));
		}
		//
		LongRunningTaskExecutor<?> taskExecutor = null;
		ResultModel resultModel = null;
		Exception ex = null;
		try {
			taskExecutor = (LongRunningTaskExecutor<?>) AutowireHelper.createBean(Class.forName(task.getTaskType()));
			taskExecutor.setLongRunningTaskId(task.getId());
			taskExecutor.init((Map<String, Object>) task.getTaskProperties());
		} catch (ClassNotFoundException e) {
			ex = e;
			resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_NOT_FOUND,
					ImmutableMap.of(
							"taskId", task.getId(),
							"taskType", task.getTaskType(),
							"instanceId", task.getInstanceId()));
		} catch (ResultCodeException e) {
			ex = e;
			resultModel = ((ResultCodeException) e).getError().getError();
		} catch (Exception e) {
			ex = e;
		}
		if (ex != null) {
			if (resultModel == null) {
				resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_INIT_FAILED,
						ImmutableMap.of(
								"taskId", task.getId(),
								"taskType", task.getTaskType(),
								"instanceId", task.getInstanceId()));
			}
			//
			LOG.error(resultModel.toString(), ex);
			task.setResult(new OperationResult.Builder(OperationState.EXCEPTION).setModel(resultModel).setCause(ex).build());
			service.save(task);
			return null;
		} else {
			return taskExecutor;
		}
	}

	private void cancelTaskByRestart(IdmLongRunningTaskDto task) {
		LOG.info("Cancel unprocessed long running task [{}] - tasks was interrupt during instance [{}] restart", task, task.getInstanceId());
		task.setRunning(false);
		ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_CANCELED_BY_RESTART,
				ImmutableMap.of(
						"taskId", task.getId(),
						"taskType", task.getTaskType(),
						"instanceId", task.getInstanceId()));
		task.setResult(new OperationResult.Builder(OperationState.CANCELED).setModel(resultModel).build());
		service.saveInternal(task);
	}
}
