package eu.bcvsolutions.idm.core.notification.service.impl;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.service.EmailService;
import eu.bcvsolutions.idm.core.notification.service.Emailer;

/**
 * Default email sender implementation
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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
	
	// TODO: refactor to configurationService
	
	@Value("${emailer.protocol:smtp}")
	private String protocol;
	
	@Value("${emailer.host:localhost}")
	private String host;
	
	@Value("${emailer.port:25}")
	private String port;
	
	@Value("${emailer.username:username@mail.eu}")
	private String username;
	
	@Value("${emailer.password:password}")
	private String password;
	
	@Value("${emailer.from:idm@bcvsolutions.eu}")
	private String from;
	
	@Value("${emailer.test.enabled:true}")
	private boolean testEnabled;
	
	public boolean send(IdmEmailLog emailLog) {
		log.debug("Sending email [{}]", emailLog);
		
		try {
			Endpoint endpoint = camelContext.getEndpoint(MessageFormat.format("{0}://{1}:{2}?username={3}&password={4}", protocol, host, port, username, password));
			
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

			if (testEnabled) {
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
	
	private Map<String, Object> createEmailHeaders(IdmEmailLog emailLog) {
		Map<String, Object> headers = new HashMap<String, Object>();		
		// resolve recipients
		headers.put("To", getRecipiets(emailLog));		
		headers.put("From", this.from);
		// when from is given - transform to reply to
		if (emailLog.getFrom() != null && StringUtils.isNotBlank(emailLog.getFrom().getRealRecipient())) {
			headers.put("Reply-To", emailLog.getFrom().getRealRecipient());
		}
		headers.put("Subject", emailLog.getMessage().getSubject());
		
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
