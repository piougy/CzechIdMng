package eu.bcvsolutions.idm.core.notification.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;

/**
 * Repository for sent emails
 * 
 * @author Peter Sourek
 *
 */
public interface IdmSmsLogRepository extends AbstractEntityRepository<IdmSmsLog, NotificationFilter> {
	
	/**
	 * @deprecated use IdmSmsLogService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmSmsLog> find(NotificationFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmSmsLogService (uses criteria api)");
	}
	
	/**
	 * Returns sms log by given id - for internal purpose.
	 * 
	 * @param id
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
	        " where "
	        + "e.id = :id")
	IdmSmsLog get(@Param("id") UUID id);

}
