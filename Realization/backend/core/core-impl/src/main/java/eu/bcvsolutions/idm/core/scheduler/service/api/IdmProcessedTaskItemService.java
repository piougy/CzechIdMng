package eu.bcvsolutions.idm.core.scheduler.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service layer for processed task items.
 * @author Jan Helbich
 *
 */
public interface IdmProcessedTaskItemService
	extends ReadWriteDtoService<IdmProcessedTaskItemDto, IdmProcessedTaskItemFilter>, 
	AuthorizableService<IdmProcessedTaskItemDto> {

	/**
	 * Deletes all processed task items by references long running task.
	 * @param dto
	 */
	void deleteAllByLongRunningTask(IdmLongRunningTaskDto dto);
	
	/**
	 * Deletes all processed task items by references scheduled task.
	 * @param dto
	 */
	void deleteAllByScheduledTask(IdmScheduledTaskDto dto);
	
	/**
	 * Find all referenced entity identifiers by scheduled tasks.
	 * @param dto
	 * @return
	 */
	List<UUID> findAllRefEntityIdsInQueueByScheduledTask(IdmScheduledTaskDto dto);
	
	/**
	 * Find all queue items of given scheduled tasks.
	 * @param scheduledTask
	 * @param pageable
	 * @return
	 */
	Page<IdmProcessedTaskItemDto> findQueueItems(IdmScheduledTaskDto scheduledTask, Pageable pageable);
	
	/**
	 * Find all log items of given long running task.
	 * @param longRunningTask
	 * @param pageable
	 * @return
	 */
	Page<IdmProcessedTaskItemDto> findLogItems(IdmLongRunningTaskDto longRunningTask, Pageable pageable);
	
	<DTO extends AbstractDto> IdmProcessedTaskItemDto createLogItem(DTO processedItem, OperationResult result, IdmLongRunningTaskDto lrt);
	
	<DTO extends AbstractDto> IdmProcessedTaskItemDto createQueueItem(DTO processedItem, OperationResult result, IdmScheduledTaskDto st);

}
