package eu.bcvsolutions.idm.core.notification.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.SynchronizationAdapter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.config.domain.EmailerConfiguration;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.SendOperation;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.Emailer;
import eu.bcvsolutions.idm.core.notification.api.service.IdmEmailLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;

/**
 * Default email sender implementation
 * 
 * @author Radek Tomiška 
 *
 */
@Component("emailer")
public class DefaultEmailer implements Emailer {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEmailer.class);
	
	@Autowired private CamelContext camelContext;
	@Autowired private IdmEmailLogService emailLogService;
	@Autowired private ProducerTemplate producerTemplate;
	@Autowired private EmailerConfiguration configuration;
	@Autowired private IdmNotificationTemplateService notificationTemplateService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private EntityEventManager entityEventManager;

	@Transactional
	public boolean send(IdmEmailLogDto emailLog) {
		LOG.debug("Sending email [{}]", emailLog);
		
		if (ObjectUtils.isEmpty(emailLog.getRecipients())) {
			LOG.info("Email recipiets is empty. Email [{}] is logged only.", emailLog);
			emailLogService.setEmailSentLog(emailLog.getId(), "Email recipients is empty. Email was logged only.");
			return false;
		}
		try {
			Endpoint endpoint = configureEndpoint();
			//
			// If message contain template transform message, otherwise get simple message
			IdmMessageDto idmMessage = notificationTemplateService.buildMessage(emailLog.getMessage(), true);
			
			// create the exchange with the mail message that is multipart with a file and a Hello World text/plain message.
			Exchange exchange = endpoint.createExchange();
			Message in = exchange.getIn();
			in.setHeaders(createEmailHeaders(emailLog));
			// message - html has higher priority
			String message = idmMessage.getHtmlMessage();
			if (StringUtils.isEmpty(message)) {
				message = idmMessage.getTextMessage();
			}
			in.setBody(message);
			
			/* TODO: attachment preparations
			DataSource ds = new javax.mail.util.ByteArrayDataSource("test txt content", "text/plain; charset=UTF-8");
			in.addAttachment("rest.txt", new DataHandler(ds));
			*/
			
			entityEventManager.publishEvent(new DefaultSendOperation(emailLog, endpoint, exchange));
			
			return true;
		} catch(Exception ex) {
			LOG.error("Sending email [{}] failed: [{}]", emailLog, ex);
			emailLogService.setEmailSentLog(emailLog.getId(), StringUtils.abbreviate(ex.toString(), DefaultFieldLengths.LOG));
			return false;
		}

	}
	
	/**
	 * Email will be send by event after original transaction ends
	 * 
	 * @param sendOperation
	 */
	@TransactionalEventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendInternal(SendOperation sendOperation) {
		if (configuration.isTestEnabled()) {
			LOG.info("Test mode for emailer is enabled. Email [{}] will be logged only.", sendOperation.getEmailLog());
			emailLogService.setEmailSentLog(sendOperation.getEmailLog().getId(), "Test mode for emailer was enabled. Email was logged only.");
		} else {
			LOG.debug("Email was registered to producer [{}]", sendOperation.getEmailLog());
			producerTemplate.asyncCallback(sendOperation.getEndpoint(), sendOperation.getExchange(), new EmailCallback(sendOperation.getEmailLog().getId(), emailLogService));
		}
	}
	
	/**
	 * Configure apache camel endpoint for email by configuration
	 * @return
	 */
	private Endpoint configureEndpoint() {
		StringBuilder endpoint = new StringBuilder(MessageFormat.format("{0}://{1}:{2}", configuration.getProtocol(), configuration.getHost(), configuration.getPort()));
		// append credentials [optional]
		String username = configuration.getUsername();
		if (StringUtils.isNotBlank(username)) {		
			// password is optional too, but null cannot be given - empty stryng is given instead
			endpoint.append(MessageFormat.format(
					"?username={0}&password={1}", 
					username, 
					configuration.getPassword() == null ? "" : configuration.getPassword().asString())
					);
		}		
		return camelContext.getEndpoint(endpoint.toString());
	}
	
	private Map<String, Object> createEmailHeaders(IdmEmailLogDto emailLog) {
		Map<String, Object> headers = new HashMap<String, Object>();		
		// resolve recipients
		headers.put("To", getRecipiets(emailLog));	

		String from = emailLog.getMessage().getTemplate().getSender();
		if (StringUtils.isBlank(from)) {
			from = configuration.getFrom();
		}

		if (StringUtils.isNotBlank(from)) {
			headers.put("From", from);
		}
		// when from is given - transform to reply to
		if (emailLog.getIdentitySender() != null) {
			String fromEmail = emailLogService.getEmailAddress(identityService.get(emailLog.getIdentitySender()));
			if (StringUtils.isNotBlank(fromEmail)) {
				headers.put("Reply-To", fromEmail);
			}			
		}
		headers.put("Subject", emailLog.getMessage().getSubject());
		headers.put("contentType", "text/html;charset=UTF-8");
		
		return headers;
	}
	
	/**
	 * Returns filled recipients email addresses joined with comma
	 * 
	 * @param emailLog
	 * @return
	 */
	private String getRecipiets(IdmEmailLogDto emailLog) {
		Assert.notNull(emailLog, "EmailLog is required!");
		//
		return emailLog.getRecipients().stream()
				.map(recipient -> recipient.getRealRecipient())
				.filter(recipient -> StringUtils.isNotBlank(recipient))
				.collect(Collectors.joining(EMAILS_SEPARATOR));
	}
	
	/**
	 * Private implementation interface {@link SendOperation} for transfer object to email event
	 * 
	 * @author Ondrej Kopr <kopr@xyxy.cz>
	 *
	 */
	private static class DefaultSendOperation implements SendOperation {
		private final IdmEmailLogDto emailLog;
		private final Endpoint endpoint;
		private final Exchange exchange;
		
		public DefaultSendOperation(IdmEmailLogDto emailLog, Endpoint endpoint, Exchange exchange) {
			this.emailLog = emailLog;
			this.endpoint = endpoint;
			this.exchange = exchange;
		}
		
		public IdmEmailLogDto getEmailLog() {
			return emailLog;
		}

		public Endpoint getEndpoint() {
			return endpoint;
		}
		
		public Exchange getExchange() {
			return exchange;
		}
	}
	
	/**
	 * Our own callback that will gather all the responses.
	 * We extend the SynchronizationAdapter class as we then only need to override the onFailure and onComplete method.
	 */
	private static class EmailCallback extends SynchronizationAdapter {
		
		private final UUID emailLogId;
		private final IdmEmailLogService emailLogService;
		
		public EmailCallback(UUID emailLogId, IdmEmailLogService emailLogService) {
			this.emailLogId = emailLogId;
			this.emailLogService = emailLogService;
		}
		
		@Override
	    public void onFailure(Exchange exchange) {			
			LOG.error("Sending email [id:{}] failed: [{}]", emailLogId, exchange.getException()); // exception cannot be null here
			emailLogService.setEmailSentLog(emailLogId, StringUtils.abbreviate(exchange.getException().toString(), DefaultFieldLengths.LOG));
		}		
		
	    @Override
	    public void onComplete(Exchange exchange) {
	    	try {
	    		LOG.info("Sending email [id:{}] succeeded", emailLogId);
		    	emailLogService.setEmailSent(emailLogId, new DateTime());
	    	} catch (Exception ex) {
	    		LOG.error("Unspecified error while save emailLog, with id: [{}].", emailLogId, ex);
	    		throw ex;
	    	}
	    }
	}
}
