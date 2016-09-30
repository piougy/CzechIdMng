package eu.bcvsolutions.idm.notification.service.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.SynchronizationAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.config.domain.EmailerConfiguration;
import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.service.EmailService;
import eu.bcvsolutions.idm.notification.service.Emailer;

/**
 * Default email sender implementation
 * 
 * @author Radek Tomiška 
 *
 */
@Component("emailer")
public class DefaultEmailer implements Emailer {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultEmailer.class);
	
	@Autowired
	private CamelContext camelContext;
	
	@Autowired 
	private EmailService emailService;

	@Autowired
    private ProducerTemplate producerTemplate;
	
	@Autowired
	private EmailerConfiguration configuration;
	
	public boolean send(IdmEmailLog emailLog) {
		log.debug("Sending email [{}]", emailLog);
		
		if (ObjectUtils.isEmpty(emailLog.getRecipients())) {
			log.info("Email recipiets is empty. Email [{}] is logged only.", emailLog);
			emailService.setEmailSentLog(emailLog.getId(), "Email recipiets is empty. Email was logged only.");
			return false;
		}
		
		try {
			Endpoint endpoint = configureEndpoint();
			
			// create the exchange with the mail message that is multipart with a file and a Hello World text/plain message.
			Exchange exchange = endpoint.createExchange();
			Message in = exchange.getIn();
			in.setHeaders(createEmailHeaders(emailLog));
			// TODO: textMessage 
			in.setBody(emailLog.getMessage().getHtmlMessage());
			
			/* TODO: attachment preparations
			DataSource ds = new javax.mail.util.ByteArrayDataSource("test txt content", "text/plain; charset=UTF-8");
			in.addAttachment("rest.txt", new DataHandler(ds));
			*/

			if (configuration.isTestEnabled()) {
				log.info("Test mode for emailer is enabled. Email [{}] is logged only.", emailLog);
				emailService.setEmailSentLog(emailLog.getId(), "Test mode for emailer was enabled. Email was logged only.");
			} else {
				log.debug("Email was registered to producer [{}]", emailLog);
				producerTemplate.asyncCallback(endpoint, exchange, new EmailCallback(emailLog.getId(), emailService));
			}
			return true;
		} catch(Exception ex) {
			log.error("Sending email [{}] failed: [{}]", emailLog, ex);
			emailService.setEmailSentLog(emailLog.getId(), StringUtils.abbreviate(ex.toString(), DefaultFieldLengths.LOG));
			return false;
		}
	}
	
	/**
	 * Configure apache camel endpoint for email by configuration
	 * @return
	 */
	private Endpoint configureEndpoint() {
		StringBuilder endpoint = new StringBuilder(MessageFormat.format("{0}://{1}:{2}", configuration.getProtocol(), configuration.getHost(), configuration.getPort()));
		// append principals
		String username = configuration.getUsername();
		if (StringUtils.isNotBlank(username)) {
			endpoint.append(MessageFormat.format("?username={0}&password={1}", username, configuration.getPassword()));
		}		
		return camelContext.getEndpoint(endpoint.toString());
	}
	
	private Map<String, Object> createEmailHeaders(IdmEmailLog emailLog) {
		Map<String, Object> headers = new HashMap<String, Object>();		
		// resolve recipients
		headers.put("To", getRecipiets(emailLog));	
		
		String from = configuration.getFrom();
		if (StringUtils.isNotBlank(from)) {
			headers.put("From", from);
		}
		// when from is given - transform to reply to
		if (emailLog.getSender() != null) {
			String fromEmail = emailService.getEmailAddress(emailLog.getSender());
			if (StringUtils.isNotBlank(fromEmail)) {
				headers.put("Reply-To", fromEmail);
			}			
		}
		headers.put("Subject", emailLog.getMessage().getSubject());
		headers.put("contentType", "text/html");
		
		return headers;
	}
	
	/**
	 * Returns filled recipients email addresses joined with comma
	 * 
	 * @param notification
	 * @return
	 */
	private String getRecipiets(IdmEmailLog emailLog) {
		Assert.notNull(emailLog, "EmailLog is required!");
		//
		return emailLog.getRecipients().stream()
				.map(recipient -> recipient.getRealRecipient())
				.filter(recipient -> StringUtils.isNotBlank(recipient))
				.collect(Collectors.joining(EMAILS_SEPARATOR));
	}
	
	/**
	 * Our own callback that will gather all the responses.
	 * We extend the SynchronizationAdapter class as we then only need to override the onFailure and onComplete method.
	 */
	private static class EmailCallback extends SynchronizationAdapter {
		
		private final Long emailLogId;
		private final EmailService emailService;
		
		public EmailCallback(Long emailLogId, EmailService emailService) {
			this.emailLogId = emailLogId;
			this.emailService = emailService;
		}
		
		@Override
	    public void onFailure(Exchange exchange) {			
			log.error("Sending email [id:{}] failed: [{}]", emailLogId, exchange.getException()); // exception can not be null here
			emailService.setEmailSentLog(emailLogId, StringUtils.abbreviate(exchange.getException().toString(), DefaultFieldLengths.LOG));
		}		
		
	    @Override
	    public void onComplete(Exchange exchange) {
	    	log.info("Sending email [id:{}] succeeded", emailLogId);
	    	emailService.setEmailSent(emailLogId, new Date());
	    }
	}
}
