package eu.bcvsolutions.idm.core.scheduler.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;

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
 * @deprecated since 7.6.0, use {@link eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor}
 */
@Deprecated
public abstract class AbstractSchedulableStatefulExecutor<DTO extends AbstractDto>
	extends eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor<DTO> {
	
	@Autowired protected IdmProcessedTaskItemService itemService;
}
