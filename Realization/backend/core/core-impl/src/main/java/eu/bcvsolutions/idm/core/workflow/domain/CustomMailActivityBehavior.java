package eu.bcvsolutions.idm.core.workflow.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.service.EmailNotificationSender;

/**
 * Custom Mail implementation
 * 
 * @author svanda
 * @author Radek Tomiška
 */
public class CustomMailActivityBehavior extends MailActivityBehavior {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomMailActivityBehavior.class);

	private transient EmailNotificationSender emailService;
	private transient IdmIdentityService identityService;

	/**
	 * Sending email through {@link EmailNotificationSender}.
	 */
	@Override
	public void execute(DelegateExecution execution) {
		LOG.trace("Sending email from workflow execution [{}]", execution.getId());
		IdmEmailLogDto emailLog = new IdmEmailLogDto();
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
		emailLog.setIdentitySender(getIdentity(getStringFromField(from, execution)).getId());
		// message
		emailLog.setMessage(new IdmMessageDto.Builder()
				.setSubject(getStringFromField(subject, execution))
				.setTextMessage(getStringFromField(text, execution))
				.setHtmlMessage(getStringFromField(html, execution))
				.build());

		IdmNotificationDto result = emailService.send(emailLog);
		LOG.trace("Email from workflow execution [{}] was sent with result [{}]", execution.getId(), result == null ? false : true);
		
		leave(execution);
	}

	private IdmNotificationRecipientDto prepareRecipient(String identityOrAddress) {
		Assert.hasText(identityOrAddress, "Identity username or email address is required.");
		//
		IdmIdentityDto identity = getIdentity(identityOrAddress);
		if (identity != null) {
			return new IdmNotificationRecipientDto(identity.getId());
		}
		return new IdmNotificationRecipientDto(identityOrAddress);
	}

	private IdmIdentityDto getIdentity(String identity) {
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
