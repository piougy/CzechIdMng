package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization log repository
 * 
 * @author Svanda
 *
 */
public interface SysSyncLogRepository extends AbstractEntityRepository<SysSyncLog> {

	@Query(value = "select e from SysSyncLog e"+ 
			" where" +
	        " (?#{[0].synchronizationConfigId} is null or e.synchronizationConfig.id = ?#{[0].synchronizationConfigId})"+
			" AND"+
	        " (?#{[0].running} is null or e.running = ?#{[0].running})"
			)
	Page<SysSyncLog> find(SysSyncLogFilter filter, Pageable pageable);
}
