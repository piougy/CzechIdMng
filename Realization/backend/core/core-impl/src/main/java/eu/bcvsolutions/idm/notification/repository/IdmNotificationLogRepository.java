package eu.bcvsolutions.idm.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Repository for notification system
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "notifications", //
	path = "notifications", //
	itemResourceRel = "notification",
	exported = false
)
public interface IdmNotificationLogRepository extends AbstractEntityRepository<IdmNotificationLog, NotificationFilter> {
	
	@Override
	@Query(value = "select e from IdmNotificationLog e left join e.identitySender s" +
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
        		+ "?#{[0].state} is null "
        		+ "or (?#{[0].state} = 'NOT' and not exists(from IdmNotification rn where rn.parent = e and rn.sent is not null)) "
        		+ "or (?#{[0].state} = 'ALL' and not exists(from IdmNotification rn where rn.parent = e and rn.sent is null) and exists(from IdmNotification rn where rn.parent = e)) "
        		+ "or (?#{[0].state} = 'PARTLY' and exists(from IdmNotification rn where rn.parent = e and rn.sent is null) and exists(from IdmNotification rn where rn.parent = e and rn.sent is not null)) "
        	+ ") "
        	+ "and "
        	+ "(?#{[0].from == null ? 'null' : ''} = 'null' or e.created >= ?#{[0].from}) "
        	+ "and "
        	+ "(?#{[0].till == null ? 'null' : ''} = 'null' or e.created <= ?#{[0].till})")
	Page<IdmNotificationLog> find(NotificationFilter filter, Pageable pageable);
}
