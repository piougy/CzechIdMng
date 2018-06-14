package eu.bcvsolutions.idm.core.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.LongRunningTaskProcessor;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask_;
import eu.bcvsolutions.idm.core.scheduler.event.processor.LongRunningTaskEndProcessor;

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

	protected final static String RESULT_PROPERTY = "result";
	//
	@Autowired private LongRunningTaskEndProcessor endProcessor;
	// observe task id = quartz task id
	private static Map<String, CountDownLatch> listenTasks = new ConcurrentHashMap<>();
	private static Map<String, OperationResult> results = new ConcurrentHashMap<>();
	private static Map<String, String> resultValues = new ConcurrentHashMap<>();
		
	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		String taskId = getTaskId(event.getContent());
		OperationResult result = event.getContent().getResult();
		// TODO: event result should contain result value
		String resultValue = (String) event.getContent().getTaskProperties().get(RESULT_PROPERTY);
		//
		listenTasks.get(taskId).countDown();
		results.put(taskId, result);
		resultValues.put(taskId, resultValue);
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
		IdmScheduledTaskDto scheduledTask = DtoUtils.getEmbedded(longRunningTask, IdmLongRunningTask_.scheduledTask, (IdmScheduledTaskDto) null);
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
	}
	
	public static OperationResult getResult(String taskId) {
		return results.get(taskId);
	}
	
	public static String getResultValue(String taskId) {
		return resultValues.get(taskId);
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
	 * Before provisioning
	 */
	@Override
	public int getOrder() {
		return endProcessor.getOrder() + 1;
	}
}
