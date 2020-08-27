package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Identity's password provisioning and setup echos for managed systems
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Identity's and all selected systems password provisioning and setup echo for managed systems.")
public class IdentityPasswordProvisioningProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-password-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProvisioningProcessor.class);
	private final ProvisioningService provisioningService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private PasswordFilterManager passwordFilterManager;
	
	@Autowired
	public IdentityPasswordProvisioningProcessor(ProvisioningService provisioningService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(provisioningService, "Service is required.");
		//
		this.provisioningService = provisioningService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto, "Password change dto is required.");
		GuardedString password = passwordChangeDto.getNewPassword();
		List<UUID> givenManagedAccounts = this.getListProperty(PasswordFilterManager.MANAGED_ACCOUNTS, event, UUID.class);
		//
		// Process echos for managed accounts
		List<UUID> managedAccounts = Lists.newArrayList();
		for (String accountIdAsString : passwordChangeDto.getAccounts()) {
			UUID accountId = UUID.fromString(accountIdAsString);
			
			if (isAccountManaged(accountId, givenManagedAccounts)) {
				managedAccounts.add(accountId);
				passwordFilterManager.createEcho(accountId, password);
			}
		}
		//
		LOG.debug("Call provisioning for identity password [{}]", event.getContent().getUsername());
		List<OperationResult> results = provisioningService.changePassword(identity, passwordChangeDto);
		//
		// Process failed provisioning and remove echos
		for (OperationResult result : results) {
			// Continue only for executed
			if (result.getState() == OperationState.EXECUTED) {
				continue;
			}
			ResultModel model = result.getModel();
			IdmAccountDto account = (IdmAccountDto) model.getParameters().get(IdmAccountDto.PARAMETER_NAME);
			UUID accountId = account.getId();

			// Clear echo only for all managed account that hasn't provisioning operation in EXECUTED state
			if (managedAccounts.contains(accountId)) {
				passwordFilterManager.clearChangedEcho(account.getId());
			}
		}
		//
		return new DefaultEventResult.Builder<>(event, this).setResults(results).build();
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}

	/**
	 * Check if given account is managed by password filter (echo must be created). Managed account can be given by second paramter.
	 * If parameter will be null managed account will be checked by new DB query.
	 *
	 * @param accountId
	 * @param managedAccounts
	 * @return
	 */
	private boolean isAccountManaged(UUID accountId, List<UUID> managedAccounts) {
		if (CollectionUtils.isEmpty(managedAccounts)) {
			AccAccountFilter filter = new AccAccountFilter();
			filter.setId(accountId);
			filter.setSupportPasswordFilter(Boolean.TRUE);
			return accountService.count(filter) != 0;
		}
		return managedAccounts.contains(accountId);
	}
}