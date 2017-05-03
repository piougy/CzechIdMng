package eu.bcvsolutions.idm.core.notification.service.impl;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationRecipientRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationRecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Notification recipient service
 *
 * @author Peter Šourek
 */
@Service
public class DefaultIdmNotificationRecipientService extends AbstractReadWriteDtoService<IdmNotificationRecipientDto, IdmNotificationRecipient, NotificationRecipientFilter> implements IdmNotificationRecipientService {

    @Autowired
    public DefaultIdmNotificationRecipientService(IdmNotificationRecipientRepository repository) {
        super(repository);
    }

}
