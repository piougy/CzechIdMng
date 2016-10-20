package eu.bcvsolutions.idm.notification.repository;

import java.util.Date;

import javax.persistence.TemporalType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.notification.domain.NotificationGroupPermission;
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
	itemResourceRel = "notification"
)
public interface IdmNotificationLogRepository extends BaseRepository<IdmNotificationLog, EmptyFilter> {
	
	@Override
	@Query(value = "select e from IdmNotificationLog e")
	@RestResource(exported = false)
	Page<IdmNotificationLog> find(EmptyFilter filter, Pageable pageable);
	
	// TODO: refactor using jpa criteria - is not possible to use named parameters now (because optional date parameters) and readability is lost ... 
	@Query(value = "select e from IdmNotificationLog e left join e.sender s" +
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
        		+ "or lower(s.username) like ?#{[1] == null ? '%' : [1].toLowerCase()} "
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[2]} is null "
        		+ "or exists (from IdmNotificationRecipient nr where nr.notification = e and lower(nr.identityRecipient.username) like ?#{[2] == null ? '%' : [2].toLowerCase()})"
        	+ ") "
        	+ "and "
        	+ "("
        		+ "?#{[3]} is null "
        		+ "or (?#{[3]} = 'NOT' and not exists(from IdmNotification rn where rn.parent = e and rn.sent is not null)) "
        		+ "or (?#{[3]} = 'ALL' and not exists(from IdmNotification rn where rn.parent = e and rn.sent is null) and exists(from IdmNotification rn where rn.parent = e)) "
        		+ "or (?#{[3]} = 'PARTLY' and exists(from IdmNotification rn where rn.parent = e and rn.sent is null) and exists(from IdmNotification rn where rn.parent = e and rn.sent is not null)) "
        	+ ") "
        	+ "and "
        	+ "(?#{[4] == null ? 'null' : ''} = 'null' or e.created >= ?#{[4]}) "
        	+ "and "
        	+ "(?#{[5] == null ? 'null' : ''} = 'null' or e.created <= ?#{[5]})")
	@RestResource(path = "quick", rel = "quick")
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Page<IdmNotificationLog> findByQuick(
			@Param(value = "text") String text,
			@Param(value = "sender") String sender,
			@Param(value = "recipient") String recipient,
			@Param(value = "sent") String sent,
			@Param(value = "createdFrom") @Temporal(TemporalType.TIMESTAMP) @DateTimeFormat(iso = ISO.DATE) Date createdFrom,
			@Param(value = "createdTill") @Temporal(TemporalType.TIMESTAMP) @DateTimeFormat(iso = ISO.DATE) Date createdTill,
			Pageable pageable);
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Iterable<IdmNotificationLog> findAll();
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Page<IdmNotificationLog> findAll(Pageable pageable);
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Iterable<IdmNotificationLog> findAll(Sort sort);
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	IdmNotificationLog findOne(@Param("id") Long id);
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_WRITE + "')")
	IdmNotificationLog save(@Param("entity") IdmNotificationLog entity);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmNotificationLog entity);
}
