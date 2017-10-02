package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization item log repository
 * 
 * @author Svanda
 *
 */
public interface SysSyncItemLogRepository extends AbstractEntityRepository<SysSyncItemLog> {
	
	@Query(value = "select e from SysSyncItemLog e"+ 
			" where" +
	        " (?#{[0].syncActionLogId} is null or e.syncActionLog.id = ?#{[0].syncActionLogId})" +
	        " and" +
	        " (lower(e.displayName) like ?#{[0].displayName == null ? '%' : '%'.concat([0].displayName.toLowerCase()).concat('%')})"
			)
	Page<SysSyncItemLog> find(SysSyncItemLogFilter filter, Pageable pageable);
}
