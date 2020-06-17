package eu.bcvsolutions.idm.core.model.event.processor.delegation;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.DelegationDefinitionProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.DelegationDefinitionEvent.DelegationDefinitionEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Processor for send notification on create a new delegation definition.
 *
 * @author Vít Švanda
 *
 */
@Component(DelegationDefinitionCreateNotificationProcessor.PROCESSOR_NAME)
@Description("Processor for send notification on create a new delegation definition.")
public class DelegationDefinitionCreateNotificationProcessor extends CoreEventProcessor<IdmDelegationDefinitionDto> implements DelegationDefinitionProcessor {

	public static final String PROCESSOR_NAME = "delegation-def-create-notification-processor";

	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private IdmConfigurationService configurationService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private SecurityService securityService;

	@Autowired
	public DelegationDefinitionCreateNotificationProcessor() {
		super(DelegationDefinitionEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmDelegationDefinitionDto> process(EntityEvent<IdmDelegationDefinitionDto> event) {
		IdmDelegationDefinitionDto dto = event.getContent();

		Assert.notNull(dto, "Delegate definition cannot be null!");
		Assert.notNull(dto.getType(), "Delegate type cannot be null!");
		Assert.notNull(dto.getDelegator(), "Delegator cannot be null!");
		Assert.notNull(dto.getDelegate(), "Delegate cannot be null!");

		// Transform dates
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(ConfigurationService.DEFAULT_APP_DATE_FORMAT);
		LocalDate validFrom = dto.getValidFrom();
		String from = "-∞";
		if (validFrom != null) {
			from = validFrom.format(dateFormat);
		}
		LocalDate validTill = dto.getValidTill();
		String till = "∞";
		if (validTill != null) {
			till = validTill.format(dateFormat);
		}

		IdmIdentityDto delegator = identityService.get(dto.getDelegator());
		IdmIdentityDto delegate = identityService.get(dto.getDelegate());
		UUID currentUserId = securityService.getCurrentId();
		
		// Send notification to the delegate.
		senNotification(CoreModuleDescriptor.TOPIC_DELEGATION_CREATED_TO_DELEGATE, dto, delegator, delegate, from, till, delegate);
		// Send notification to the delegator (only if the delegator didn't create the delegation).
		if (!dto.getDelegator().equals(currentUserId)) {
			senNotification(CoreModuleDescriptor.TOPIC_DELEGATION_CREATED_TO_DELEGATOR, dto, delegator, delegate, from, till, delegator);
		}

		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Send notification
	 * 
	 * @param topic
	 * @param dto
	 * @param delegator
	 * @param delegate
	 * @param from
	 * @param till
	 * @param recipient 
	 */
	private void senNotification(String topic, IdmDelegationDefinitionDto dto,
			IdmIdentityDto delegator, IdmIdentityDto delegate,
			String from, String till, IdmIdentityDto recipient) {

		notificationManager.send(topic,
				new IdmMessageDto.Builder().setLevel(NotificationLevel.SUCCESS)
						.addParameter("delegation", dto)
						.addParameter("delegator", delegator)
						.addParameter("delegate", delegate)
						.addParameter("url", configurationService
								.getFrontendUrl(String.format("identity/%s/delegation-definitions/%s/detail", recipient.getId(), dto.getId())))
						.addParameter("from", from)
						.addParameter("till", till).build(), recipient);
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
