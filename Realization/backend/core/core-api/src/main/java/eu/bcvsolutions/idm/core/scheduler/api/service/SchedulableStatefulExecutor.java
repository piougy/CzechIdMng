package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;

/**
 * Basic interface for stateful task executors, which need to keep track of already
 * processed items.
 * 
 * @author Jan Helbich
 *
 * @param <E> entity DTO type
 * @param <V> return value type
 */
public interface SchedulableStatefulExecutor<E extends AbstractDto, V> extends SchedulableTaskExecutor<V> {
	
	/**
	 * Name of the workflow variable which stores the operation result
	 * of type {@link OperationResult}.
	 */
	public static final String OPERATION_RESULT_VAR = "operationResult";
	
	/**
	 * Returns a pageable result set of DTOs the task should process.
	 * @param pageable
	 * @return
	 */
	Page<E> getItemsToProcess(Pageable pageable);
	
	/**
	 * Processes single entity DTO. If operation result is not returned,
	 * the worker shall handle queue item addition or removal itself, i.e.
	 * by an asynchronous callback.
	 * 
	 * @param dto
	 * @return
	 */
	Optional<OperationResult> processItem(E dto);
	
	/**
	 * Returns all entity references (of type {@link AbstractDto#getId()}
	 * from processed items queue, including items that were processed in earlier
	 * runs of this executor.
	 * 
	 * @return a collection of entity ID references
	 */
	Collection<UUID> getProcessedItemRefsFromQueue();
	
	/**
	 * Checks whether the given entity DTO has been already processed
	 * and is active in processed items queue.
	 * 
	 * @param dto
	 * @return
	 */
	boolean isInProcessedQueue(E dto);
	
	/**
	 * Adds entity DTO among processed items.
	 * 
	 * @param dto
	 */
	IdmProcessedTaskItemDto addToProcessedQueue(E dto, OperationResult opResult);
	
	/**
	 * Removes entity reference from active items in processed queue by its ID.
	 * 
	 * @param entityRef
	 */
	void removeFromProcessedQueue(UUID entityRef);
	
	/**
	 * Removes entity reference from active items in processed queue.
	 * 
	 * @param dto
	 */
	void removeFromProcessedQueue(E dto);

	/**
	 * Logs the operation result of given entity DTO processing.
	 * 
	 * @param dto
	 * @param opResult
	 */
	IdmProcessedTaskItemDto logItemProcessed(E dto, OperationResult opResult);
	
}
