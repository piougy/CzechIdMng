package eu.bcvsolutions.idm.core.audit.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventProperty;

/**
 * Default repository for {@link IdmLoggingEventProperty}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventPropertyRepository extends AbstractEntityRepository<IdmLoggingEventProperty> {
	
	/**
	 * Removes all logging event properties by event id
	 * 
	 * @param eventId
	 * @return
	 */
	@Modifying
	@Query("DELETE FROM #{#entityName} e WHERE eventId = :eventId")
	int deleteByEventId(@Param("eventId") Long eventId);
}
