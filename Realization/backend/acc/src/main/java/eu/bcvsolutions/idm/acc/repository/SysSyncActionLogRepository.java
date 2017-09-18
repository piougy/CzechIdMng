package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization action log repository
 * 
 * @author Svanda
 *
 */
public interface SysSyncActionLogRepository extends AbstractEntityRepository<SysSyncActionLog> {
	
	@Query(value = "select e from SysSyncActionLog e"+ 
			" where" +
	        " (?#{[0].synchronizationLogId} is null or e.syncLog.id = ?#{[0].synchronizationLogId})"
			)
	Page<SysSyncActionLog> find(SyncActionLogFilter filter, Pageable pageable);
}
