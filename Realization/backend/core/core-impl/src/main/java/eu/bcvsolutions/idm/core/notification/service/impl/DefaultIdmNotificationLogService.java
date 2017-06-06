package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification_;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Notification log service
 * 
 * @author Radek Tomiška
 *
 */
@Service("notificationLogService")
public class DefaultIdmNotificationLogService 
		extends AbstractNotificationLogService<IdmNotificationLogDto, IdmNotificationLog, NotificationFilter> 
		implements IdmNotificationLogService {

	@Autowired
	public DefaultIdmNotificationLogService(IdmNotificationLogRepository repository, ModelMapper modelMapper) {
		super(repository);		
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(NotificationGroupPermission.NOTIFICATION, getEntityClass());
	}
	
	/**
	 * TODO: can be moved to AbstractNotificationLogService?
	 */
	@Override
	protected List<Predicate> toPredicates(Root<IdmNotificationLog> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, NotificationFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			Path<IdmMessage> message = root.get(IdmNotification_.message);
			predicates.add(builder.or(
					builder.like(builder.lower(message.get(IdmMessage_.subject)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(message.get(IdmMessage_.textMessage)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(message.get(IdmMessage_.htmlMessage)), "%" + filter.getText().toLowerCase() + "%")				
					));
		}
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmNotification_.created), filter.getTill()));
		}
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmNotification_.created), filter.getFrom()));
		}
		if (filter.getSender() != null) {
			predicates.add(builder.equal(root.get(IdmNotification_.identitySender).get(IdmIdentity_.username), filter.getSender()));
		}
		if (filter.getRecipient() != null) {
			Subquery<IdmNotificationRecipient> subquery = query.subquery(IdmNotificationRecipient.class);
			Root<IdmNotificationRecipient> subRoot = subquery.from(IdmNotificationRecipient.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmNotificationRecipient_.notification), root), // correlation attr
                    		builder.equal(subRoot.get(IdmNotificationRecipient_.identityRecipient).get(IdmIdentity_.username), filter.getRecipient())
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		if (filter.getState() != null) {
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
				predicates.add(builder.not(builder.exists(sent)));
			} else {
				Subquery<IdmNotification> notSent = query.subquery(IdmNotification.class);
				Root<IdmNotification> notSentRoot = notSent.from(IdmNotification.class);
				notSent.select(notSentRoot);
				notSent.where(
	                    builder.and(
	                    		builder.equal(notSentRoot.get(IdmNotification_.parent), root), // correlation attr
	                    		builder.isNull(notSentRoot.get(IdmNotification_.sent))
	                    		)
	            );
				if (filter.getState() == NotificationState.PARTLY) {
					predicates.add(builder.and(builder.exists(notSent), builder.exists(sent)));
				} else { // all
					predicates.add(builder.and(builder.not(builder.exists(notSent)), builder.exists(sent)));
				}
			}		
		}
		if (filter.getNotificationType() != null)  {
			predicates.add(builder.equal(root.type(), builder.literal(filter.getNotificationType())));
		}
		if (filter.getParent() != null) {
			predicates.add(builder.equal(root.get(IdmNotification_.parent).get(IdmNotification_.id), filter.getParent()));
		}
		return predicates;
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
		if(!entity.getType().equals(IdmNotificationLog.NOTIFICATION_TYPE)) {
			return dto;
		}
		// sent state by related notifications
		boolean hasSent = false;
		boolean hasNotSent = false;
		for(IdmNotification relatedNotification : entity.getRelatedNotifications()) {
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
}
