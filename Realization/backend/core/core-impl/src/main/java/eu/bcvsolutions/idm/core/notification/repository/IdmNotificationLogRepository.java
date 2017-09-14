package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;

/**
 * Repository for notification system
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmNotificationLogRepository extends AbstractEntityRepository<IdmNotificationLog> {

	@Override
	@Query("select count(e) from IdmNotificationLog e where TYPE(e) = IdmNotificationLog")
	long count();
}
