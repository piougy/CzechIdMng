package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;

/**
 * Sending emails to queue (email will be sent asynchronously)
 * 
 * @author Radek Tomiška 
 *
 */
public interface EmailNotificationSender extends NotificationSender<IdmEmailLogDto> {
	
	/**
	 * Method send email and save notification to log
	 * 
	 * @param message
	 * @param emails
	 * @return
	 */
	IdmEmailLogDto send(IdmMessageDto message, String email);
	
	/**
	 * Method send email and save notification log
	 * 
	 * @param message
	 * @param emails
	 * @return
	 */
	IdmEmailLogDto send(IdmMessageDto message, String[] emails);
	
	/**
	 * Method send email and save notification to log
	 * 
	 * @param message
	 * @param emails
	 * @param attachments Attachments - can be instanced (inputData will be used, if it's not {@code null}), or persisted - data will be loaded by attachment manager.
	 * @return
	 * @since 9.3.0
	 */
	IdmEmailLogDto send(IdmMessageDto message, String[] emails, List<IdmAttachmentDto> attachments);
}
