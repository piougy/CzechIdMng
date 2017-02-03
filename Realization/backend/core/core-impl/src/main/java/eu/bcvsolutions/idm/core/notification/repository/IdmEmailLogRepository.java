package eu.bcvsolutions.idm.core.notification.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;

/**
 * Repository for sent emails
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "emails", //
	path = "emails", //
	itemResourceRel = "email",
	exported = false
)
public interface IdmEmailLogRepository extends AbstractEntityRepository<IdmEmailLog, NotificationFilter> {
	
	@Override
	@Query(value = "select e from IdmEmailLog e left join e.identitySender s" +
	        " where "
	        + "("
	        	+ "?#{[0].text} is null "
	        	+ "or lower(e.message.subject) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
	        	+ "or lower(e.message.textMessage) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
	        	+ "or lower(e.message.htmlMessage) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[0].sender} is null "
        		+ "or lower(s.username) like ?#{[0].sender == null ? '%' : [0].sender.toLowerCase()} "
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[0].recipient} is null "
        		+ "or exists (from IdmNotificationRecipient nr where nr.notification = e and lower(nr.identityRecipient.username) like ?#{[0].recipient == null ? '%' : [0].recipient.toLowerCase()})"
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[0].sent} is null "
        		+ "or (?#{[0].sent} = false and e.sent is null) "
        		+ "or (?#{[0].sent} = true and e.sent is not null)"
        	+ ") "
        	+ "and "
        	+ "(?#{[0].from == null ? 'null' : ''} = 'null' or e.created >= ?#{[0].from}) "
        	+ "and "
        	+ "(?#{[0].till == null ? 'null' : ''} = 'null' or e.created <= ?#{[0].till})")
	Page<IdmEmailLog> find(NotificationFilter filter, Pageable pageable);
	
	/**
	 * Returns email log by given id - for internal purpose.
	 * 
	 * @param name
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
	        " where "
	        + "e.id = :id")
	IdmEmailLog get(@Param("id") UUID id);

}
