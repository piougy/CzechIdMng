package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;

/**
 * Identity's password change notification
 * 
 * @author Patrik Stloukal
 * @author Radek Tomiška
 *
 */
@Component
@Description("Sends notification to identity of changed password.")
public class IdentityPasswordChangeNotificationProcessor extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-password-change-notification";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private NotificationManager notificationManager;

	public IdentityPasswordChangeNotificationProcessor() {
		super(IdentityEventType.PASSWORD);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		List<EventResult<IdmIdentityDto>> results = event.getContext().getResults();
		//
		List<IdmAccountDto> successAccounts = new ArrayList<>();
		List<OperationResult> failureResults = new ArrayList<>();	
		List<String> systemNames = new ArrayList<>();
		for (EventResult<IdmIdentityDto> eventResult : results) {
			eventResult.getResults().forEach(result -> {
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
		}
		//
		// send notification if at least one system success
		if (!successAccounts.isEmpty()) {
			notificationManager.send(
					CoreModuleDescriptor.TOPIC_PASSWORD_CHANGED,
					new IdmMessageDto.Builder()
					.setLevel(NotificationLevel.SUCCESS)
					.addParameter("successSystemNames", StringUtils.join(systemNames, ", "))
					.addParameter("successAccounts", successAccounts)
					.addParameter("failureResults", failureResults)
					.addParameter("name", identityService.getNiceLabel(identity))
					.addParameter("username", identity.getUsername())
					.build(),
					identity);
		}		
		//
		return new DefaultEventResult<>(event, this);

	}

	@Override
	public int getOrder() {
		return super.getOrder() + 1500;
	}
}
