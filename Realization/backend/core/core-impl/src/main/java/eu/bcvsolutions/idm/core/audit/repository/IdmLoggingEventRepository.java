package eu.bcvsolutions.idm.core.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent;

/**
 * Repository for logging event
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventRepository extends AbstractEntityRepository<IdmLoggingEvent, LoggingEventFilter> {
	
	/**
	 * @deprecated use IdmLoggingEventService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from LoggingEvent e")
	default Page<IdmLoggingEvent> find(LoggingEventFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmLoggingEventService (uses criteria api)");
	}
	
	/**
	 * Method find one {@link IdmLoggingEvent} by unique id - event id.
	 * @param id
	 * @return
	 */
	IdmLoggingEvent findOneById(@Param(value = "id") Long id);
}
