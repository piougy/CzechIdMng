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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationRecipientService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient_;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationRecipientRepository;


/**
 * Notification recipient service.
 *
 * @author Radek Tomiška
 * @author Peter Šourek
 */
@Service
public class DefaultIdmNotificationRecipientService 
		extends AbstractReadWriteDtoService<IdmNotificationRecipientDto, IdmNotificationRecipient, IdmNotificationRecipientFilter> 
		implements IdmNotificationRecipientService {
	
    @Autowired
    public DefaultIdmNotificationRecipientService(IdmNotificationRecipientRepository repository) {
        super(repository);
    }

    @Override
    protected List<Predicate> toPredicates(Root<IdmNotificationRecipient> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmNotificationRecipientFilter filter) {
    	List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
    	//
    	// quick - "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.like(builder.lower(root.get(IdmNotificationRecipient_.realRecipient)), "%" + text + "%"));
		}
		UUID notification = filter.getNotification();
		if (notification != null) {
			predicates.add(builder.equal(root.get(IdmNotificationRecipient_.notification).get(AbstractEntity_.id), notification));
		}
		//
		UUID identityRecipient = filter.getIdentityRecipient();
		if (identityRecipient != null) {
			predicates.add(builder.equal(root.get(IdmNotificationRecipient_.identityRecipient).get(IdmIdentity_.id), identityRecipient));
		}
		//
		String realRecipient = filter.getRealRecipient();
		if (StringUtils.isNotEmpty(realRecipient)) {
			predicates.add(builder.equal(root.get(IdmNotificationRecipient_.realRecipient), realRecipient));
		}
    	//
    	return predicates;
    }
}
