package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest_;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.workflow.config.WorkflowConfig;

/**
 * Processor for send notification after request was realized on systems
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleRequestNotificationProcessor.PROCESSOR_NAME)
@Description(" Processor for send notification after request was realized on systems")
public class RoleRequestNotificationProcessor extends CoreEventProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {
	public static final String PROCESSOR_NAME = "role-request-notification-processor";

	@Autowired
	private IdmConfigurationService configurationService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private NotificationManager notificationManager;

	public RoleRequestNotificationProcessor() {
		super(RoleRequestEventType.CREATE, RoleRequestEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto dto = event.getContent();
		// Notification is not send if request is execute immediately
		if (dto == null || dto.isExecuteImmediately()) {
			return false;
		}
		
		if (dto != null
				&& (dto.getSystemState() == null 
				|| OperationState.EXECUTED == dto.getSystemState().getState())) {
			return super.conditional(event);
		}

		return false;
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto dto = event.getContent();
		IdmRoleRequestDto originalSource = event.getOriginalSource();
		Assert.notNull(dto, "DTO is required for role request processing.");

		OperationResultDto systemState = dto.getSystemState();
		OperationResultDto originalSystemState = originalSource != null ? originalSource.getSystemState() : null;

		if (systemState != null && OperationState.EXECUTED == systemState.getState()
				&& !(originalSystemState != null && OperationState.EXECUTED == originalSystemState.getState())) {
			// State was changed -> send notification
			this.sendNotification(dto);
		} else if (systemState == null && originalSystemState != null) {
			// State was changed to null (request does not have any provisioning operations) -> send notification
			this.sendNotification(dto);
		}
				

		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Send notification to applicant and implementer
	 * 
	 * @param request
	 */
	private void sendNotification(IdmRoleRequestDto request) {
		UUID requestId = request.getId();
		Assert.notNull(requestId, "Role request has to persisted before notification is sent.");

		boolean sendNotificationToApplicant = configurationService
				.getBooleanValue(WorkflowConfig.SEND_NOTIFICATION_TO_APPLICANT_CONFIGURATION_PROPERTY, true);
		boolean sendNotificationToImplementer = configurationService
				.getBooleanValue(WorkflowConfig.SEND_NOTIFICATION_TO_IMPLEMENTER_CONFIGURATION_PROPERTY, false);

		// Transform created date
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(ConfigurationService.DEFAULT_APP_DATETIME_FORMAT);
		String from = request.getCreated().format(dateFormat);

		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		Set<IdmConceptRoleRequestDto> addedRoles = concepts.stream() //
				.filter(concept -> ConceptRoleRequestOperation.ADD == concept.getOperation()) //
				.collect(Collectors.toSet()); //

		Set<IdmConceptRoleRequestDto> changedRoles = concepts.stream() //
				.filter(concept -> ConceptRoleRequestOperation.UPDATE == concept.getOperation()) //
				.collect(Collectors.toSet()); //

		Set<IdmConceptRoleRequestDto> removedRoles = concepts.stream() //
				.filter(concept -> ConceptRoleRequestOperation.REMOVE == concept.getOperation()) //
				.collect(Collectors.toSet()); //

		IdmIdentityDto applicantIdentity = DtoUtils.getEmbedded(request, IdmRoleRequest_.applicant.getName(),
				IdmIdentityDto.class);
		IdmIdentityDto implementerIdentity = identityService.get(request.getCreatorId());

		if (implementerIdentity.equals(applicantIdentity)) {
			// Send notification only to implementer if
			// implementer and applicant is same identity
			if (sendNotificationToImplementer || sendNotificationToApplicant) {
				send(CoreModuleDescriptor.TOPIC_REQUEST_REALIZED_IMPLEMENTER, request, from, addedRoles, changedRoles,
						removedRoles, applicantIdentity, implementerIdentity);
			}
		} else {
			// Send notification to applicant
			if (sendNotificationToApplicant) {
				send(CoreModuleDescriptor.TOPIC_REQUEST_REALIZED_APPLICANT, request, from, addedRoles, changedRoles, removedRoles,
						applicantIdentity, applicantIdentity);
			}
			// Send notification to implementer
			if (sendNotificationToImplementer) {
				send(CoreModuleDescriptor.TOPIC_REQUEST_REALIZED_IMPLEMENTER, request, from, addedRoles, changedRoles,
						removedRoles, applicantIdentity, implementerIdentity);
			}
		}
	}

	/**
	 * Send notification to specific topic
	 * 
	 * @param topic
	 * @param from
	 * @param addedRoles
	 * @param changedRoles
	 * @param removedRoles
	 * @param applicantIdentity
	 */
	private void send(String topic, IdmRoleRequestDto request, String from, Set<IdmConceptRoleRequestDto> addedRoles,
			Set<IdmConceptRoleRequestDto> changedRoles, Set<IdmConceptRoleRequestDto> removedRoles,
			IdmIdentityDto applicantIdentity, IdmIdentityDto recipientIdentity) {
		notificationManager.send(topic, new IdmMessageDto.Builder().setLevel(NotificationLevel.SUCCESS)//
				.addParameter("addedRoles", addedRoles)//
				.addParameter("changedRoles", changedRoles)//
				.addParameter("removedRoles", removedRoles)//
				.addParameter("identity", applicantIdentity)//
				.addParameter("url", this.getUrl(request))//
				.addParameter("from", from).build(), recipientIdentity);
	}
	
	/**
	 * Construct URL to frontend for given request
	 * 
	 * @param request
	 * @return
	 */
	private String getUrl(IdmRoleRequestDto request) {
		if (request == null) {
			return null;
		}
		String origins = configurationService.getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
		//
		if (origins != null && !origins.isEmpty()) {
			String origin = origins.trim().split(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN_SEPARATOR)[0];
			return MessageFormat.format("{0}/#/role-requests/{1}/detail", origin, request.getId());
		}
		return null;
	}
	
	@Override
	public int getOrder() {
		// After save
		return 1000;
	}
}
