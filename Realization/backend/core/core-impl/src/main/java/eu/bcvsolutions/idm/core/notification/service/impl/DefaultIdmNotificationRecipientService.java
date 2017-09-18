package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationRecipientRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationRecipientService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;


/**
 * Notification recipient service
 *
 * @author Peter Šourek
 */
@Service
public class DefaultIdmNotificationRecipientService 
		extends AbstractReadWriteDtoService<IdmNotificationRecipientDto, IdmNotificationRecipient, NotificationRecipientFilter> 
		implements IdmNotificationRecipientService {

	private IdmNotificationRecipientRepository repository;
	
    @Autowired
    public DefaultIdmNotificationRecipientService(IdmNotificationRecipientRepository repository) {
        super(repository);
        //
        this.repository = repository;
    }

    @Override
	protected Page<IdmNotificationRecipient> findEntities(NotificationRecipientFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
}
