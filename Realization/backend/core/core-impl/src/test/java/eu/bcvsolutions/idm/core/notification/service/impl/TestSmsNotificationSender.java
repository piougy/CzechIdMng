package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmSmsLogService;

/**
 * Test sms sender.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Service
public class TestSmsNotificationSender extends AbstractSmsNotificationSender {

	private IdmSmsLogService idmSmsLogService;
	
	public TestSmsNotificationSender(IdmSmsLogService idmSmsLogService, IdmIdentityService identityService) {
		super(idmSmsLogService, identityService);
		//
		this.idmSmsLogService = idmSmsLogService;
	}
	
	@Override
	public Class<? extends BaseEntity> getNotificationType() {
		return idmSmsLogService.getEntityClass();
	}

	@Override
	public List<IdmSmsLogDto> sendSms(IdmNotificationDto notification) {
		return notification
				.getRecipients()
				.stream()
				.map(r -> {
					return this.createLog(notification);
				})
				.collect(Collectors.toList());
	}


}
