package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.Collections;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;

/**
 * Notification log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmNotificationLogService 
		extends AbstractNotificationLogService<IdmNotificationLogDto, IdmNotificationLog, NotificationFilter> 
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
}
