package eu.bcvsolutions.idm.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmWebsocketLog;

/**
 * Repository for sent messages through websocket
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "websocketLogs", //
	path = "websocket-logs", //
	itemResourceRel = "websocketLog",
	exported = false
)
public interface IdmWebsocketLogRepository extends AbstractEntityRepository<IdmWebsocketLog, NotificationFilter> {
	
	@Override
	@Query(value = "select e from IdmWebsocketLog e left join e.identitySender s" +
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
	Page<IdmWebsocketLog> find(NotificationFilter filter, Pageable pageable);
}
