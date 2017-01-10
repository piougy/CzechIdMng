package eu.bcvsolutions.idm.core.workflow.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.service.api.EmailNotificationSender;

/**
 * Custom Mail implementation
 * 
 * @author svanda, tomiska
 */
public class CustomMailActivityBehavior extends MailActivityBehavior {

	private static final long serialVersionUID = 1L;
	private static final transient Logger log = LoggerFactory.getLogger(CustomMailActivityBehavior.class);

	private transient EmailNotificationSender emailService;
	private transient IdmIdentityService identityService;

	/**
	 * Sending emails through {@link EmailNotificationSender}
	 */
	@Override
	public void execute(ActivityExecution execution) {
		log.trace("Sending email from workflow execution [{}]", execution.getId());
		IdmEmailLog emailLog = new IdmEmailLog();
		// recipients
		String[] tos = splitAndTrim(getStringFromField(to, execution));
		if (!ObjectUtils.isEmpty(tos)) {
			emailLog.setRecipients(
					Arrays.stream(tos)
					.map(identityOrAddress -> {
						return prepareRecipient(identityOrAddress);
						})
					.collect(Collectors.toList()));
		}
		// sender
		emailLog.setIdentitySender(getIdentity(getStringFromField(from, execution)));
		// message
		emailLog.setMessage(new IdmMessage.Builder()
				.setSubject(getStringFromField(subject, execution))
				.setTextMessage(getStringFromField(text, execution))
				.setHtmlMessage(getStringFromField(html, execution))
				.build());

		IdmNotification result = emailService.send(emailLog);
		log.trace("Email from workflow execution [{}] was sent with result [{}]", execution.getId(), result == null ? false : true);
		
		leave(execution);
	}

	private IdmNotificationRecipient prepareRecipient(String identityOrAddress) {
		Assert.hasText(identityOrAddress);
		//
		IdmIdentity identity = getIdentity(identityOrAddress);
		if (identity != null) {
			return new IdmNotificationRecipient(identity);
		}
		return new IdmNotificationRecipient(identityOrAddress);
	}

	private IdmIdentity getIdentity(String identity) {
		if (StringUtils.isEmpty(identity)) {
			return null;
		}
		return identityService.getByUsername(identity);
	}

	public void setEmailService(EmailNotificationSender emailService) {
		this.emailService = emailService;
	}

	public void setIdentityService(IdmIdentityService identityService) {
		this.identityService = identityService;
	}

}
