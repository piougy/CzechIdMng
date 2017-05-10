package eu.bcvsolutions.idm.core.scheduler.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmProcessedTaskItem;

/**
 * Processed task items repository.
 * @author Jan Helbich
 *
 */
public interface IdmProcessedTaskItemRepository
	extends AbstractEntityRepository<IdmProcessedTaskItem, IdmProcessedTaskItemFilter> {

	
	@Override
	@Deprecated
	default Page<IdmProcessedTaskItem> find(IdmProcessedTaskItemFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmProcessedItemsQueueService (uses criteria api)");
	}
	
	@Transactional
	@Modifying
	@Query("delete from #{#entityName} e where e.longRunningTask.id = :lrtId")
	void deleteAllByLongRunningTaskId(@Param("lrtId") UUID id);

	@Transactional
	@Modifying
	@Query("delete from #{#entityName} e where e.scheduledTaskQueueOwner.id = :stId")
	void deleteAllByScheduledTaskId(@Param("stId") UUID id);
	
	@Query("select e.referencedEntityId from #{#entityName} e where e.scheduledTaskQueueOwner.id = :stId")
	List<UUID> findAllRefEntityIdsByScheduledTaskId(@Param("stId") UUID id);

}
