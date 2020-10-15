package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationAttachmentDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationAttachmentFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationAttachmentService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationAttachment;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationAttachment_;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationAttachmentRepository;


/**
 * Notification attachment service.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Service
public class DefaultIdmNotificationAttachmentService 
		extends AbstractReadWriteDtoService<IdmNotificationAttachmentDto, IdmNotificationAttachment, IdmNotificationAttachmentFilter> 
		implements IdmNotificationAttachmentService {
	
    @Autowired
    public DefaultIdmNotificationAttachmentService(IdmNotificationAttachmentRepository repository) {
        super(repository);
    }

    @Override
    protected List<Predicate> toPredicates(Root<IdmNotificationAttachment> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmNotificationAttachmentFilter filter) {
    	List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
    	//
    	// quick - "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.like(builder.lower(root.get(IdmNotificationAttachment_.name)), "%" + text + "%"));
		}
		UUID notification = filter.getNotification();
		if (notification != null) {
			predicates.add(builder.equal(root.get(IdmNotificationAttachment_.notification).get(AbstractEntity_.id), notification));
		}
    	//
    	return predicates;
    }
}
