package eu.bcvsolutions.idm.core.scheduler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Schedulable task services (this services will be automatically available as scheduled tasks)
 * 
 * @author Radek Tomiška
 * @author Jan Helbich
 * @depreceted since 7.6.0, use {@link eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor}
 */
@Deprecated
public abstract class AbstractSchedulableTaskExecutor<V> 
		extends eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor<V> {
	
	@Autowired protected IdmLongRunningTaskService service;
}
