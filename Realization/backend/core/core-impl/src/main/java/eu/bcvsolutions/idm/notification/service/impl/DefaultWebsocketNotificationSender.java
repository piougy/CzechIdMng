package eu.bcvsolutions.idm.notification.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.notification.api.dto.FlashMessage;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.notification.service.api.IdmWebsocketLogService;
import eu.bcvsolutions.idm.notification.service.api.WebsocketNotificationSender;

/**
 * Send messages throgh websocket
 * 
 * @author Radek Tomiška
 *
 */
@Component("websocketNotificationSender")
public class DefaultWebsocketNotificationSender extends AbstractNotificationSender<IdmWebsocketLog> implements WebsocketNotificationSender {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultWebsocketNotificationSender.class);
	private final IdmWebsocketLogService websocketLogService;
	private final SimpMessagingTemplate websocket;
    
    @Autowired
    public DefaultWebsocketNotificationSender(
    		IdmWebsocketLogService websocketLogService,
    		SimpMessagingTemplate websocket) {
    	Assert.notNull(websocket);
    	Assert.notNull(websocketLogService);
    	//
		this.websocket = websocket;
		this.websocketLogService = websocketLogService;
	}
	
	@Override
	@Transactional
	public IdmWebsocketLog send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		LOG.info("Adding email notification to queue [{}]", notification);
		IdmWebsocketLog log = createLog(notification);
		// send flashmessage
		FlashMessage message = toFlashMessage(log);
		boolean sent = false;
		for (IdmNotificationRecipient recipient : log.getRecipients()) {
			websocket.convertAndSendToUser(
					recipient.getRealRecipient(),
					"/queue/messages", // TODO: configurable
					message);
		}
		return sent ? log : null;
	}
	
	/**
	 * Returns recipient's username, if identity is defined and realRecipient is defined, then realRecipient is returned (has higher priority).
	 *  
	 * @param recipient
	 * @return
	 */
	public String getUsername(IdmNotificationRecipient recipient) {
		if (recipient == null) {
			return null;
		}
		if (StringUtils.isNotBlank(recipient.getRealRecipient())) {
			return recipient.getRealRecipient();
		}
		
		if (recipient.getIdentityRecipient() != null) {
			return recipient.getIdentityRecipient().getUsername();
		}
		return null;
	}	
	
	/**
	 * Persists new emailog record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmWebsocketLog createLog(IdmNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		//
		// we can only create log, if notification is instance of IdmNotificationLog
		if (notification instanceof IdmWebsocketLog) {
			notification.setSent(new DateTime());
			return websocketLogService.save((IdmWebsocketLog) notification);
		}
		//
		IdmWebsocketLog log = new IdmWebsocketLog();
		log.setSent(new DateTime());
		// parent message
		if (notification.getId() != null) {
			log.setParent(notification);
		}
		// clone message
		log.setMessage(cloneMessage(notification));
		// clone recipients - resolve real email
		notification.getRecipients().forEach(recipient -> {
			log.getRecipients().add(cloneRecipient(log, recipient));
		});
		log.setIdentitySender(notification.getIdentitySender());
		return websocketLogService.save(log);
	}
	
	/**
	 * clone recipients with resolved real email address
	 * 
	 * @param notification - recipients new parent
	 * @param recipient - source recipient
	 * @return
	 */
	@Override
	protected IdmNotificationRecipient cloneRecipient(IdmNotification notification, IdmNotificationRecipient recipient) {
		return new IdmNotificationRecipient(notification, recipient.getIdentityRecipient(), getUsername(recipient));
	}
	
	private FlashMessage toFlashMessage(IdmWebsocketLog log) {
		IdmMessage message = log.getMessage();
		//
		FlashMessage flashMessage = new FlashMessage();
		flashMessage.setId(log.getId());
		flashMessage.setCode(message.getSubject());
		flashMessage.setMessage(message.getTextMessage()); // default message
		flashMessage.setDate(log.getCreated());
		flashMessage.setLevel(message.getLevel() == null ? null : message.getLevel().toString().toLowerCase());
		//
		flashMessage.setParameters(message.getParameters());
		flashMessage.setPosition("tr"); // TODO: from parameters
		flashMessage.setKey(null); // TODO: from parameters
		flashMessage.setHidden(false); // TODO: from parameters
		return flashMessage;
	}
}
