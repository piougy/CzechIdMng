package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;

/**
 * Repository for notification system
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "notifications", //
	path = "notifications", //
	itemResourceRel = "notification"
)
public interface IdmNotificationLogRepository extends BaseRepository<IdmNotificationLog> {
	
	@Query(value = "select e from IdmNotificationLog e" +
	        " where" +
	        " (:text is null "
	        	+ "or lower(e.message.subject) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')} "
	        	+ "or lower(e.message.textMessage) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')} "
	        	+ "or lower(e.message.htmlMessage) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')} "
        	+ ")")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmNotificationLog> findByQuick(@Param(value = "text") String text, Pageable pageable);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmNotificationLog entity);

}
