package eu.bcvsolutions.idm.core.scheduler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Schedulable task services (this services will be automatically available as scheduled tasks)
 * 
 * @author Radek Tomiška
 * @author Jan Helbich
 * @depreceted use {@link eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor}
 */
@Deprecated
public abstract class AbstractSchedulableTaskExecutor<V> 
		extends eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor<V> {

	@Autowired protected SecurityService securityService;
	@Autowired protected IdmLongRunningTaskService service;
	@Autowired protected IdmLongRunningTaskService longRunningTaskService;
	@Autowired protected IdmScheduledTaskService scheduledTaskService;
}
