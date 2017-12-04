package eu.bcvsolutions.idm.core.scheduler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Template for long running task executor. This template persists long running tasks.
 * 
 * @author Radek Tomiška
 * @deprecated since 7.6.0, use {@link eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor}
 */
public abstract class AbstractLongRunningTaskExecutor<V> extends eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor<V> {
	
	@Autowired protected IdmLongRunningTaskService service;
}
