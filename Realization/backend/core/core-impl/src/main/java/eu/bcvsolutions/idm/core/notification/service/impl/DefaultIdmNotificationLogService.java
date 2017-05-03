package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class DefaultIdmNotificationLogService extends AbstractNotificationLogService<IdmNotificationLogDto, IdmNotificationLog, NotificationFilter> implements IdmNotificationLogService {

	private final ModelMapper modelMapper;
	
	@Autowired
	public DefaultIdmNotificationLogService(IdmNotificationLogRepository repository, ModelMapper modelMapper) {
		super(repository);		
		this.modelMapper = modelMapper;
	}

	@Override
	public List<IdmNotificationRecipientDto> getReciipientsForNotification(String backendId) {
		final IdmNotificationLog entity = get(backendId);
		return entity.getRecipients()
				.stream()
				.map(r -> modelMapper.map(r, IdmNotificationRecipientDto.class))
				.collect(Collectors.toList());
	}
}
