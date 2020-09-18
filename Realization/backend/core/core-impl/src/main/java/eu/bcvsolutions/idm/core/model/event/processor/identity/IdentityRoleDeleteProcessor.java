package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Delete identity role.
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes identity role from repository.")
public class IdentityRoleDeleteProcessor 
		extends CoreEventProcessor<IdmIdentityRoleDto> 
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-delete-processor";
	//
	@Autowired private IdmIdentityRoleService service;
	@Autowired private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;

	public IdentityRoleDeleteProcessor() {
		super(IdentityRoleEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		UUID identityRoleId = identityRole.getId();
		Assert.notNull(identityRoleId, "Content identifier is required.");
		
		// Find all concepts and remove relation on identity role
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setIdentityRoleId(identityRoleId);
		conceptRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"IdentityRole [{0}] (reqested in concept [{1}]) was deleted (not from this role request)!",
						identityRoleId, concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested IdentityRole [{1}] was deleted (not from this role request)!",
						concept.getId(), identityRoleId);
				concept = conceptRequestService.cancel(concept);
			}
			conceptRequestService.addToLog(concept, message);
			concept.setIdentityRole(null);
			conceptRequestService.save(concept);
		});
		//
		// remove all IdentityRoleValidRequest for this role
		List<IdmIdentityRoleValidRequestDto> validRequests = identityRoleValidRequestService.findAllValidRequestForIdentityRoleId(identityRoleId);
		identityRoleValidRequestService.deleteAll(validRequests);
		//
		// remove sub roles - just for sure, if role is not removed by role request
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setDirectRoleId(identityRoleId);
		service
			.find(filter, null)
			.forEach(subIdentityRole -> {
				IdentityRoleEvent subEvent = new IdentityRoleEvent(IdentityRoleEventType.DELETE, subIdentityRole);
				//
				service.publish(subEvent, event);
				// Notes identity-accounts to ACM
				notingIdentityAccountForDelayedAcm(event, subEvent);
			});
		//
		// Delete identity role
		service.deleteInternal(identityRole);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Method for noting identity-accounts for delayed account management
	 * 
	 * @param event
	 * @param subEvent
	 */
	@SuppressWarnings("unchecked")
	private void notingIdentityAccountForDelayedAcm(EntityEvent<IdmIdentityRoleDto> event,
			EntityEvent<IdmIdentityRoleDto> subEvent) {
		Assert.notNull(event, "Event is required.");
		Assert.notNull(subEvent, "Sub event is required.");

		if (!event.getProperties().containsKey(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM)) {
			event.getProperties().put(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM, new HashSet<UUID>());
		}

		Set<UUID> identityAccounts = (Set<UUID>) subEvent.getProperties()
				.get(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM);
		if (identityAccounts != null) {
			((Set<UUID>) event.getProperties().get(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM))
				.addAll(identityAccounts);
		}
	}
}
