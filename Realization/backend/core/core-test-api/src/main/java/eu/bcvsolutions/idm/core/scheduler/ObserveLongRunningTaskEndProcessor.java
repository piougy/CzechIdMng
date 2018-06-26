package eu.bcvsolutions.idm.core.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.LongRunningTaskProcessor;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;

/**
 * Listen LongRunningTask ends
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ObserveLongRunningTaskEndProcessor
		extends CoreEventProcessor<IdmLongRunningTaskDto> 
		implements LongRunningTaskProcessor {

	public final static String RESULT_PROPERTY = "result";
	//
	// observe task id = quartz task id
	private static Map<String, CountDownLatch> listenTasks = new ConcurrentHashMap<>();
	private static Map<String, OperationResult> results = new ConcurrentHashMap<>();
	private static Map<String, String> resultValues = new ConcurrentHashMap<>();
	private static Map<String, IdmLongRunningTaskDto> longRunningTasks = new ConcurrentHashMap<>();
		
	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		String taskId = getTaskId(event.getContent());
		OperationResult result = event.getContent().getResult();
		// TODO: event result should contain result value
		String resultValue = (String) event.getContent().getTaskProperties().get(RESULT_PROPERTY);
		//
		longRunningTasks.put(taskId, event.getContent());
		results.put(taskId, result);
		if (resultValue != null) {
			resultValues.put(taskId, resultValue);
		}
		//
		// has to be at the end - waiting has to be ended after results are set
		listenTasks.get(taskId).countDown();
		//
		// TODO: event result should contain result value, it's too important, so I'm saying it twice :)
		return null;
	}
	
	@Override
	public boolean supports(EntityEvent<?> event) {
		if (!super.supports(event)) {
			return false;
		}
		String taskId = getTaskId((IdmLongRunningTaskDto) event.getContent());
		if (taskId == null) {
			return false;
		}
		//
		return listenTasks.keySet().contains(taskId);
	}
	
	private String getTaskId(IdmLongRunningTaskDto longRunningTask) {
		IdmScheduledTaskDto scheduledTask = DtoUtils.getEmbedded(longRunningTask, "scheduledTask", (IdmScheduledTaskDto) null);
		if (scheduledTask == null) {
			// not schedulable
			return null;
		}
		return scheduledTask.getQuartzTaskName();
	}
	
	public static void listenTask(String taskId) {
		listenTasks.put(taskId, new CountDownLatch(1));
		results.remove(taskId);
		resultValues.remove(taskId);
		longRunningTasks.remove(taskId);
	}
	
	public static OperationResult getResult(String taskId) {
		return results.get(taskId);
	}
	
	public static String getResultValue(String taskId) {
		return resultValues.get(taskId);
	}
	
	public static IdmLongRunningTaskDto getLongRunningTask(String taskId) {
		return longRunningTasks.get(taskId);
	}
	
	/**
	 * Wait for task ends
	 * 
	 * @throws InterruptedException
	 */
	public static void waitForEnd(String taskId) throws InterruptedException {
		listenTasks.get(taskId).await();
	}

	/**
	 * Must be after 'LongRunningTaskEndProcessor' processor!
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 1;
	}
}
