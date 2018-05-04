package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccContractSliceAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractSliceAccountFilter;
import eu.bcvsolutions.idm.acc.event.AccountEvent;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.event.ContractSliceAccountEvent.ContractSliceAccountEventType;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractSliceAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Deletes contract-slice account
 * 
 * @author Svanda
 */
@Component("accContractSliceAccountDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class ContractSliceAccountDeleteProcessor extends CoreEventProcessor<AccContractSliceAccountDto> {

	private static final String PROCESSOR_NAME = "contract-slice-account-delete-processor";
	private final AccContractSliceAccountService service;
	private final AccAccountService accountService;

	@Autowired
	public ContractSliceAccountDeleteProcessor(AccContractSliceAccountService service,
			AccAccountService accountService) {
		super(ContractSliceAccountEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(accountService);
		//
		this.service = service;
		this.accountService = accountService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccContractSliceAccountDto> process(EntityEvent<AccContractSliceAccountDto> event) {
		AccContractSliceAccountDto entity = event.getContent();
		UUID account = entity.getAccount();
		AccAccountDto accountDto = accountService.get(account);
		Assert.notNull(accountDto, "Account cannot be null!");

		boolean deleteTargetAccount = (boolean) event.getProperties()
				.get(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY);

		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		AccContractSliceAccountFilter filter = new AccContractSliceAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccContractSliceAccountDto> entityAccounts = service.find(filter, null).getContent();
		boolean moreEntityAccounts = entityAccounts.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && !treeAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreEntityAccounts && entity.isOwnership()) {
			// We delete all entity accounts first
			entityAccounts.forEach(identityAccount -> {
				service.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.publish(new AccountEvent(AccountEventType.DELETE, accountService.get(account),
					ImmutableMap.of(AccAccountService.DELETE_TARGET_ACCOUNT_PROPERTY, deleteTargetAccount,
							AccAccountService.ENTITY_ID_PROPERTY, entity.getEntity())));
		}

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}
