package eu.bcvsolutions.idm.core.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException;

public interface IdmLoggingEventExceptionRepository extends AbstractEntityRepository<IdmLoggingEventException, LoggingEventExceptionFilter> {

	/**
	 * @deprecated use IdmLoggingEventExceptionService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from LoggingEventException e")
	default Page<IdmLoggingEventException> find(LoggingEventExceptionFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmLoggingEventExceptionService (uses criteria api)");
	}
	
	/**
	 * Method find all exception for event id
	 * @param event
	 * @param pageable
	 * @return
	 */
	Page<IdmLoggingEventException> findAllByEvent(@Param(value = "event") Long event, Pageable pageable);
	
}
