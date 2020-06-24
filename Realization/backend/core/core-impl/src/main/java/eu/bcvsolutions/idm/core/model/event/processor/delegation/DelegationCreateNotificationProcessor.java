package eu.bcvsolutions.idm.core.model.event.processor.delegation;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.DelegationProcessor;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.service.DelegationType;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation_;
import eu.bcvsolutions.idm.core.model.event.DelegationEvent.DelegationEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;

/**
 * Processor for send notification on create a new delegation.
 *
 * @author Vít Švanda
 *
 */
@Component(DelegationCreateNotificationProcessor.PROCESSOR_NAME)
@Description("Processor for send notification on create a new delegation.")
public class DelegationCreateNotificationProcessor extends CoreEventProcessor<IdmDelegationDto> implements DelegationProcessor {

	public static final String PROCESSOR_NAME = "delegation-create-notification-processor";

	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private IdmConfigurationService configurationService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private DelegationManager delegationManager;
	@Autowired
	private LookupService lookupService;

	@Autowired
	public DelegationCreateNotificationProcessor() {
		super(DelegationEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmDelegationDto> process(EntityEvent<IdmDelegationDto> event) {
		IdmDelegationDto dto = event.getContent();
		Assert.notNull(dto, "Delegate cannot be null!");

		IdmDelegationDefinitionDto definition = lookupService.lookupEmbeddedDto(dto, IdmDelegation_.definition);

		Assert.notNull(definition, "Delegate definition cannot be null!");
		Assert.notNull(definition.getType(), "Delegate type cannot be null!");
		Assert.notNull(definition.getDelegator(), "Delegator cannot be null!");
		Assert.notNull(definition.getDelegate(), "Delegate cannot be null!");

		IdmIdentityDto delegator = identityService.get(definition.getDelegator());
		IdmIdentityDto delegate = identityService.get(definition.getDelegate());
		// UUID currentUserId = securityService.getCurrentId();

		// Send notification to the delegate.
		sendNotification(CoreModuleDescriptor.TOPIC_DELEGATION_INSTANCE_CREATED_TO_DELEGATE, dto, definition, delegator, delegate, delegate);
		
		// TODO notification to delegator:
		// Send notification to the delegator (only if the delegator didn't create the delegation).
		// if (!definition.getDelegator().equals(currentUserId)) {
			// senNotification(CoreModuleDescriptor.TOPIC_DELEGATION_INSTANCE_CREATED_TO_DELEGATOR, dto, definition, delegator, delegate, delegator);
		// }

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
	private void sendNotification(String topic, IdmDelegationDto dto,
			IdmDelegationDefinitionDto definition, IdmIdentityDto delegator, IdmIdentityDto delegate, IdmIdentityDto recipient) {

		notificationManager.send(topic,
				new IdmMessageDto.Builder().setLevel(NotificationLevel.SUCCESS)
						.addParameter("instance", dto)
						.addParameter("delegation", definition)
						.addParameter("delegator", delegator)
						.addParameter("delegate", delegate)
						.addParameter("url", getOwnerUrl(dto))
						.build(), recipient);
	}

	private String getOwnerUrl(IdmDelegationDto dto) {
		if (dto.getOwnerType().equals(WorkflowTaskInstanceDto.class.getCanonicalName())) {
			return configurationService
					.getFrontendUrl(String.format("task/%s", dto.getOwnerId()));
		}
		return null;
	}

	@Override
	public boolean conditional(EntityEvent<IdmDelegationDto> event) {
		// Notification will be send only if type supports it.
		if (event.getContent() != null) {
			IdmDelegationDefinitionDto definition = lookupService.lookupEmbeddedDto(event.getContent(), IdmDelegation_.definition);
			if (definition != null) {
				DelegationType delegateType = delegationManager.getDelegateType(definition.getType());
				if (delegateType != null) {
					return delegateType.sendDelegationNotifications();
				}
			}

			return false;
		}
		return super.conditional(event);
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
