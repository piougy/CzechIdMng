package eu.bcvsolutions.idm.core.audit.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException;

/**
 * Repository for {@link IdmLoggingEventException}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmLoggingEventExceptionRepository extends AbstractEntityRepository<IdmLoggingEventException> {
	
	/**
	 * Removes all logging event exception by event
	 * 
	 * @param event
	 * @return
	 */
	@Modifying
	@Query("DELETE FROM #{#entityName} e WHERE event.id = :eventId")
	int deleteByEventId(@Param("eventId") Long eventId);
}
