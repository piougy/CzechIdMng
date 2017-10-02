package eu.bcvsolutions.idm.core.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;

/**
 * Repository for sent emails
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmEmailLogRepository extends AbstractEntityRepository<IdmEmailLog> {
	
	/**
	 * Returns email log by given id - for internal purpose.
	 * 
	 * @param id
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
	        " where "
	        + "e.id = :id")
	IdmEmailLog get(@Param("id") UUID id);

}
