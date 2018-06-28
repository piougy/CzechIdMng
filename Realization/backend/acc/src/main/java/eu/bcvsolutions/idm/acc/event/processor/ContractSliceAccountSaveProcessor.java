package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccContractSliceAccountDto;
import eu.bcvsolutions.idm.acc.event.ContractSliceAccountEvent.ContractSliceAccountEventType;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractSliceAccountService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Save contract-slice account
 * 
 * @author Svanda
 */
@Component("accContractSliceAccountSaveProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class ContractSliceAccountSaveProcessor extends CoreEventProcessor<AccContractSliceAccountDto> {

	private static final String PROCESSOR_NAME = "contract-slice-account-save-processor";
	private final AccContractSliceAccountService service;

	@Autowired
	public ContractSliceAccountSaveProcessor(AccContractSliceAccountService service, AccAccountService accountService) {
		super(ContractSliceAccountEventType.CREATE, ContractSliceAccountEventType.UPDATE);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccContractSliceAccountDto> process(EntityEvent<AccContractSliceAccountDto> event) {
		AccContractSliceAccountDto entity = event.getContent();
		UUID account = entity.getAccount();
		Assert.notNull(account, "Account cannot be null!");

		event.setContent(service.saveInternal(entity));

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
