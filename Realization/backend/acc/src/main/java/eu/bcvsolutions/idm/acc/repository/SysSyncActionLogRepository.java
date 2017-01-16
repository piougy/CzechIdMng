package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.rest.projection.SysSyncActionLogExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization action log repository
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "syncActionLogs", //
		path = "sync-action-logs", //
		itemResourceRel = "syncActionLog", //
		excerptProjection = SysSyncActionLogExcerpt.class,
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSyncActionLogRepository extends AbstractEntityRepository<SysSyncActionLog, SyncActionLogFilter> {
	@Override
	@Query(value = "select e from SysSyncActionLog e"+ 
			" where" +
	        " (?#{[0].synchronizationLogId} is null or e.syncLog.id = ?#{[0].synchronizationLogId})"
			)
	Page<SysSyncActionLog> find(SyncActionLogFilter filter, Pageable pageable);
}
