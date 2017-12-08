package eu.bcvsolutions.idm.acc.event.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
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
	
	public static final String PROCESSOR_NAME = "identity-role-add-authorities-processor";
	
	@Autowired private IdmIdentityService identityService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private IdmPasswordPolicyService passwordPolicyService;
	@Autowired private AccAccountService accountService; 
	
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
			List<OperationResult> results = identityService.passwordChange(newIdentity, passwordChangeDto);
			//
			List<IdmAccountDto> successAccounts = new ArrayList<>();
			List<OperationResult> failureResults = new ArrayList<>();	
			List<String> systemNames = new ArrayList<>();
			results.forEach(result -> {
				if (result.getModel() != null) {
					boolean success = result.getModel().getStatusEnum().equals(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS.name());
					if (success) {	
						IdmAccountDto account = (IdmAccountDto) result.getModel().getParameters().get(IdmAccountDto.PARAMETER_NAME);
						systemNames.add(account.getSystemName());
						successAccounts.add(account);
					} else {
						// exception is logged before
						failureResults.add(result);
					}
				}
			});
			//
			// send notification if at least one system success
			if (!successAccounts.isEmpty()) {
				notificationManager.send(
						AccModuleDescriptor.TOPIC_NEW_PASSWORD_ALL_SYSTEMS,
						new IdmMessageDto.Builder()
						.setLevel(NotificationLevel.SUCCESS)
						.addParameter("successSystemNames", StringUtils.join(systemNames, ", "))
						.addParameter("successAccounts", successAccounts)
						.addParameter("failureResults", failureResults)
						.addParameter("name", identityService.getNiceLabel(newIdentity))
						.addParameter("password", password)
						.build(),
						newIdentity);
			}
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
