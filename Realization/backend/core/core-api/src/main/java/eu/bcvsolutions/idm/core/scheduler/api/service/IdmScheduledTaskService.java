package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmScheduledTaskFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Scheduled tasks service.
 * 
 * @author Jan Helbich
 *
 */
public interface IdmScheduledTaskService
	extends ReadWriteDtoService<IdmScheduledTaskDto, IdmScheduledTaskFilter>, 
	AuthorizableService<IdmScheduledTaskDto> {

	/**
	 * Finds scheduled tasks by relative quartz task name.
	 * @param taskName
	 * @return
	 */
	IdmScheduledTaskDto findByQuartzTaskName(String taskName);
	
	/**
	 * Finds scheduled tasks by assigned long running task.
	 * @param lrtId
	 * @return
	 */
	IdmScheduledTaskDto findByLongRunningTaskId(UUID lrtId);

}
