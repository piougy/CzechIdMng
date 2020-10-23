package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification_;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;

/**
 * Notification log service
 * 
 * @author Radek Tomiška
 *
 */
@Service("notificationLogService")
public class DefaultIdmNotificationLogService 
		extends AbstractNotificationLogService<IdmNotificationLogDto, IdmNotificationLog, IdmNotificationFilter> 
		implements IdmNotificationLogService {

	@Autowired
	public DefaultIdmNotificationLogService(IdmNotificationLogRepository repository, ModelMapper modelMapper) {
		super(repository);		
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmNotificationRecipientDto> getRecipientsForNotification(String backendId) {
		IdmNotificationLogDto entity = get(backendId);
		//
		if (entity == null) {
			return Collections.<IdmNotificationRecipientDto>emptyList();
		}
		//
		return entity.getRecipients();
	}
	
	@Override
	protected IdmNotificationLogDto toDto(IdmNotificationLog entity, IdmNotificationLogDto dto) {
		dto = super.toDto(entity, dto);
		if (dto == null) {
			return null;
		}
		if (!entity.getType().equals(IdmNotificationLog.NOTIFICATION_TYPE)) {
			return dto;
		}
		// sent state by related notifications
		boolean hasSent = false;
		boolean hasNotSent = false;
		// FIXME: move state check to @Formula => select is executed to each record now!
		for (IdmNotification relatedNotification : entity.getRelatedNotifications()) {
			hasSent = hasSent || (relatedNotification.getSent() != null);
			hasNotSent = hasNotSent || (relatedNotification.getSent() == null);
		}
		if (hasSent && !hasNotSent) {
			dto.setState(NotificationState.ALL);
		} else if(hasSent && hasNotSent) {
			dto.setState(NotificationState.PARTLY);
		} else {
			dto.setState(NotificationState.NOT);
		}	
		return dto;
	}
	
	/**
	 * State filter predicate.
	 * Filter is constructed differently for NotifictionLog 
	 * (PARTLY is supported) and for other single notifications by channels (email, sms etc.).
	 * 
	 * @since 10.6.0
	 */
	@Override
	protected Predicate getStatePredicate(Root<IdmNotificationLog> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmNotificationFilter filter) {
		
		if (filter == null || filter.getState() == null) {
			return null;
		}
		//
		Subquery<IdmNotification> sent = query.subquery(IdmNotification.class);
		Root<IdmNotification> sentRoot = sent.from(IdmNotification.class);
		sent.select(sentRoot);
		sent.where(
                builder.and(
                		builder.equal(sentRoot.get(IdmNotification_.parent), root), // correlation attr
                		builder.isNotNull(sentRoot.get(IdmNotification_.sent))
                		)
        );
		if (filter.getState() == NotificationState.NOT) {
			return builder.not(builder.exists(sent));
		}
		
		Subquery<IdmNotification> notSent = query.subquery(IdmNotification.class);
		Root<IdmNotification> notSentRoot = notSent.from(IdmNotification.class);
		notSent.select(notSentRoot);
		notSent.where(
                builder.and(
                		builder.equal(notSentRoot.get(IdmNotification_.parent), root), // correlation attr
                		builder.isNull(notSentRoot.get(IdmNotification_.sent))
                		)
        );
		// partly
		if (filter.getState() == NotificationState.PARTLY) {
			return builder.and(builder.exists(notSent), builder.exists(sent));
		}
		// all
		return builder.and(builder.not(builder.exists(notSent)), builder.exists(sent));
	}
}
