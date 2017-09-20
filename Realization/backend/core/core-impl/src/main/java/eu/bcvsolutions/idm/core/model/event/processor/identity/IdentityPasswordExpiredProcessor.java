package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;

/**
 * Sends warning notification after password expired.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Sends warning notification after password expired..")
public class IdentityPasswordExpiredProcessor extends CoreEventProcessor<IdmIdentityDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordExpiredProcessor.class);
	public static final String PROCESSOR_NAME = "identity-password-expired-processor";
	//
	@Autowired private IdmPasswordService passwordService;
	@Autowired private NotificationManager notificationManager;

	public IdentityPasswordExpiredProcessor() {
		super(IdentityEventType.PASSWORD_EXPIRED);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		//
		LOG.info("Sending warning notification to identity [{}], password expired in [{}]",  identity.getUsername(), password.getValidTill());
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern(getConfigurationService().getDateFormat());
		//
		notificationManager.send(
				CoreModuleDescriptor.TOPIC_PASSWORD_EXPIRED, 
				new IdmMessageDto
					.Builder(NotificationLevel.WARNING)
					.addParameter("expiration", dateFormat.print(password.getValidTill()))
					.addParameter("identity", identity)
					// TODO: where is the best place for FE urls?
					// TODO: url to password reset?
					// .addParameter("url", configurationService.getFrontendUrl(String.format("password/reset?username=%s", identity.getUsername())))
					.build(), 
				identity);
		return new DefaultEventResult<>(event, this);
	}
}
