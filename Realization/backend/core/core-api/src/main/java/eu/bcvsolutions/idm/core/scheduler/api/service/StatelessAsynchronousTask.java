package eu.bcvsolutions.idm.core.scheduler.api.service;

/**
 * "Naive" asynchronous task executor. Usable for start any asynchronous task over long running task manager - share the same thread pool.
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
public abstract class StatelessAsynchronousTask extends AbstractLongRunningTaskExecutor<Boolean> {

	@Override
	public boolean isStateful() {
		return false;
	}
}
