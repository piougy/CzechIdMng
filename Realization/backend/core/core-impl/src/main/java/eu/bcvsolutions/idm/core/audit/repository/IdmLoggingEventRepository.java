package eu.bcvsolutions.idm.core.audit.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent;

/**
 * Repository for logging event
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmLoggingEventRepository extends AbstractEntityRepository<IdmLoggingEvent> {

	/**
	 * Method find one {@link IdmLoggingEvent} by unique id - event id.
	 * 
	 * @param id
	 * @return
	 */
	IdmLoggingEvent findOneById(@Param(value = "id") Long id);

	/**
	 * Removes all logging event by id
	 * 
	 * @param id
	 * @return
	 */
	@Modifying
	@Query("DELETE FROM #{#entityName} e WHERE id = :id")
	int deleteById(@Param("id") Long id);
}
