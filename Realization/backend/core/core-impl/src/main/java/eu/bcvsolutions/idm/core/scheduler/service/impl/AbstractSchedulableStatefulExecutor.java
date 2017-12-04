package eu.bcvsolutions.idm.core.scheduler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Abstract base class for statefull tasks, which handles common
 * process flow for context-less and stateful processes (the ones
 * with inner memory.
 * 
 * All stateful processes work with entity IDs (of type UUID) as
 * references to already processed items. 
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 * @param <DTO> process DTO type, 
 */
public abstract class AbstractSchedulableStatefulExecutor<DTO extends AbstractDto>
	extends eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor<DTO> {
	
	@Autowired protected SecurityService securityService;
	@Autowired protected IdmLongRunningTaskService service;
	@Autowired protected IdmLongRunningTaskService longRunningTaskService;
	@Autowired protected IdmScheduledTaskService scheduledTaskService;
	@Autowired protected IdmProcessedTaskItemService itemService;
}
