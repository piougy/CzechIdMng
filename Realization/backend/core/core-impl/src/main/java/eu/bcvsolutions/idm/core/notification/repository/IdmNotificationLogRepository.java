package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;

/**
 * Repository for notification system
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmNotificationLogRepository extends AbstractEntityRepository<IdmNotificationLog, NotificationFilter> {
	
	/**
	 * @deprecated use IdmNotificationLogService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmNotificationLog> find(NotificationFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmNotificationLogService (uses criteria api)");
	}

	@Override
	@Query("select count(e) from IdmNotificationLog e where TYPE(e) = IdmNotificationLog")
	long count();
}
