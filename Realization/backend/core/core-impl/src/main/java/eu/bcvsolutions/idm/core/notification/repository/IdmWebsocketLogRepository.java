package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;

/**
 * Repository for sent messages through websocket
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmWebsocketLogRepository extends AbstractEntityRepository<IdmWebsocketLog, NotificationFilter> {
	
	/**
	 * @deprecated use IdmWebsocketLogService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmWebsocketLog> find(NotificationFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmWebsocketLogService (uses criteria api)");
	}
}
