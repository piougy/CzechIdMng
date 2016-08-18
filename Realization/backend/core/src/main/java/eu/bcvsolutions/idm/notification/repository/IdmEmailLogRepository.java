package eu.bcvsolutions.idm.notification.repository;

import java.util.Date;

import javax.persistence.TemporalType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Repository for sended emails
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "emails", //
	path = "emails", //
	itemResourceRel = "email"
)
public interface IdmEmailLogRepository extends BaseRepository<IdmEmailLog> {
	
	// TODO: refactor using jpa criteria - is not possible to use named parameters now (because optional date parameters) and readability is lost ... 
	@Query(value = "select e from IdmEmailLog e left join e.from.identityRecipient fr" +
	        " where "
	        + "("
	        	+ "?#{[0]} is null "
	        	+ "or lower(e.message.subject) like ?#{[0] == null ? '%' : '%'.concat([0].toLowerCase()).concat('%')} "
	        	+ "or lower(e.message.textMessage) like ?#{[0] == null ? '%' : '%'.concat([0].toLowerCase()).concat('%')} "
	        	+ "or lower(e.message.htmlMessage) like ?#{[0] == null ? '%' : '%'.concat([0].toLowerCase()).concat('%')} "
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[1]} is null "
        		+ "or lower(fr.username) like ?#{[1] == null ? '%' : [1].toLowerCase()} "
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[2]} is null "
        		+ "or exists (from IdmNotificationRecipient nr where nr.notification = e and lower(nr.identityRecipient.username) like ?#{[2] == null ? '%' : [2].toLowerCase()})"
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[3]} is null "
        		+ "or (?#{[3]} = false and e.sent is null) "
        		+ "or (?#{[3]} = true and e.sent is not null)"
        	+ ") "
        	+ "and "
        	+ "(?#{[4] == null ? 'null' : ''} = 'null' or e.created >= ?#{[4]}) "
        	+ "and "
        	+ "(?#{[5] == null ? 'null' : ''} = 'null' or e.created <= ?#{[5]})")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmNotificationLog> findByQuick(
			@Param(value = "text") String text,
			@Param(value = "sender") String sender,
			@Param(value = "recipient") String recipient,
			@Param(value = "sent") Boolean sent,
			@Param(value = "createdFrom") @Temporal(TemporalType.TIMESTAMP) @DateTimeFormat(iso = ISO.DATE) Date createdFrom,
			@Param(value = "createdTill") @Temporal(TemporalType.TIMESTAMP) @DateTimeFormat(iso = ISO.DATE) Date createdTill,
			Pageable pageable);
	
	@Override
	@RestResource(exported = false)
	<S extends IdmEmailLog> S save(S entity);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmEmailLog entity);

}
