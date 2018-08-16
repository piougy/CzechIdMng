package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordChangeNotificationProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Sets one password for all identity accounts after identity starts to be valid.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Sets one password for all identity accounts after identity starts to be valid.")
public class IdentitySetPasswordProcessor 
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {
	
	public static final String PROCESSOR_NAME = "identity-set-password-processor";
	
	@Autowired private IdmPasswordPolicyService passwordPolicyService;
	@Autowired private AccAccountService accountService; 
	@Autowired private IdentityPasswordChangeNotificationProcessor passwordChangeProcessor;
	@Autowired private EntityEventManager entityEventManager;
	
	public IdentitySetPasswordProcessor() {
		super(IdentityEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto previousIdentity = event.getOriginalSource();
		IdmIdentityDto newIdentity = event.getContent();
		if (stateStarting(previousIdentity, newIdentity) && hasAccount(newIdentity)) {
			// change password for all systems
			PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
			//
			// public password change password for all system including idm 
			passwordChangeDto.setAll(true);
			passwordChangeDto.setIdm(true);
			// TODO: how to generate password for all system policies
			GuardedString password = new GuardedString(passwordPolicyService.generatePasswordByDefault());
			passwordChangeDto.setNewPassword(password);
			// 
			// publish event for changing password
			IdentityEvent identityEvent = new IdentityEvent(
					IdentityEventType.PASSWORD,
					newIdentity, 
					ImmutableMap.of(
							IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO, passwordChangeDto,
							EntityEventManager.EVENT_PROPERTY_SKIP_NOTIFICATION, true)); // we are sending notification with newly generated password from this processor
			EventContext<IdmIdentityDto> context = entityEventManager.process(identityEvent);
			// 
			// send notification with then newly generated password
			passwordChangeProcessor.sendNotification(context, CoreModuleDescriptor.TOPIC_PASSWORD_SET, password);
		}
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Returns true, if identity has at least one account on target system, 
	 * which supports password change
	 * 
	 * @param identity
	 * @return
	 */
	private boolean hasAccount(IdmIdentityDto identity) {
		if(identity.getId() == null) {
			return false;
		}
		AccAccountFilter filter = new AccAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setSupportChangePassword(Boolean.TRUE);
		//
		return accountService.find(filter, new PageRequest(0, 1)).getTotalElements() > 0;
	}
	
	/**
	 * Return true, when identity starts to be valid
	 * 
	 * @param previousIdentity
	 * @param newIdentity
	 * @return
	 */
	private boolean stateStarting(IdmIdentityDto previousIdentity, IdmIdentityDto newIdentity) {
		return (previousIdentity.getState() == IdentityState.CREATED
				|| previousIdentity.getState() == IdentityState.NO_CONTRACT
				|| previousIdentity.getState() == IdentityState.FUTURE_CONTRACT)
				&& newIdentity.getState() == IdentityState.VALID;
	}

	/**
	 * After update
	 */
	@Override
    public int getOrder() {
    	return CoreEvent.DEFAULT_ORDER + 200;
    }
}
