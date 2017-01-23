package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.filter.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.rest.projection.SysSyncItemLogExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization item log repository
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "syncItemLogs", //
		path = "sync-item-logs", //
		itemResourceRel = "syncActionLog", //
		excerptProjection = SysSyncItemLogExcerpt.class,
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSyncItemLogRepository extends AbstractEntityRepository<SysSyncItemLog, SyncItemLogFilter> {
	@Override
	@Query(value = "select e from SysSyncItemLog e"+ 
			" where" +
	        " (?#{[0].syncActionLogId} is null or e.syncActionLog.id = ?#{[0].syncActionLogId})"
			)
	Page<SysSyncItemLog> find(SyncItemLogFilter filter, Pageable pageable);
}
