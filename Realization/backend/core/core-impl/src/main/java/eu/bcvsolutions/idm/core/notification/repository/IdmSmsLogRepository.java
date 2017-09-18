package eu.bcvsolutions.idm.core.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;

/**
 * Repository for sent emails
 * 
 * @author Peter Sourek
 *
 */
public interface IdmSmsLogRepository extends AbstractEntityRepository<IdmSmsLog> {
	
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
