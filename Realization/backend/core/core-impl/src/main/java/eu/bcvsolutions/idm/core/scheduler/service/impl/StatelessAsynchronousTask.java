package eu.bcvsolutions.idm.core.scheduler.service.impl;

/**
 * "Naive" asynchronous task executor. Usable for start any asynchronous task over long running task manager - share the same thread pool.
 * 
 * @author Radek Tomiška
 * @deprecated since 7.6.0, use {@link eu.bcvsolutions.idm.core.scheduler.api.service.StatelessAsynchronousTask}}
 */
@Deprecated
public abstract class StatelessAsynchronousTask extends AbstractLongRunningTaskExecutor<Boolean> {

	@Override
	public boolean isStateful() {
		return false;
	}
}
