package eu.bcvsolutions.idm.acc.event.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.AccContractSliceAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccContractSliceAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Before contract slice delete - deletes all contract-slice-account relations
 * 
 * @author svandav
 *
 */
@Component("accContractSliceDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class ContractSliceDeleteProcessor
		extends CoreEventProcessor<IdmContractSliceDto> 
		implements ContractSliceProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(ContractSliceDeleteProcessor.class);
	
	public static final String PROCESSOR_NAME = "contract-slice-delete-processor";
	private final AccContractSliceAccountService entityAccountService;
	
	@Autowired
	public ContractSliceDeleteProcessor(
			AccContractSliceAccountService entityAccountService) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(entityAccountService);
		//
		this.entityAccountService = entityAccountService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {

		// delete relations on account (includes delete of account	)
		AccContractSliceAccountFilter filter = new AccContractSliceAccountFilter();
		filter.setEntityId(event.getContent().getId());
		entityAccountService.find(filter, null).forEach(entityAccount -> {
			LOG.debug("Remove contract-account for account [{}]", entityAccount.getId());
			entityAccountService.delete(entityAccount);
		});
		
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before role delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}