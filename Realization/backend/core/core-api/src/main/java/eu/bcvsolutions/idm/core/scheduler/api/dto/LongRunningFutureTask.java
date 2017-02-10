package eu.bcvsolutions.idm.core.scheduler.api.dto;

import java.util.concurrent.FutureTask;

import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;

/**
 * Encapsulates future task and long running task executor
 * 
 * @author Radek Tomiška
 *
 * @param <V>
 */
public class LongRunningFutureTask<V> {

	private final LongRunningTaskExecutor<V> executor;
	private final FutureTask<V> futureTask;
	
	public LongRunningFutureTask(LongRunningTaskExecutor<V> executor, FutureTask<V> futureTask) {
		this.executor = executor;
		this.futureTask = futureTask;
	}

	public LongRunningTaskExecutor<V> getExecutor() {
		return executor;
	}
	
	public FutureTask<V> getFutureTask() {
		return futureTask;
	}
}
