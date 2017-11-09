package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent.ContractGuaranteeEventType;

/**
 * Processor delete {@link IdmContractGuaranteeDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Delete manually added gurantee for contract.")
public class ContractGuaranteeDeleteProcessor extends CoreEventProcessor<IdmContractGuaranteeDto> {

	public static final String PROCESSOR_NAME = "contract-guarantee-delete";
	
	private final IdmContractGuaranteeService contractGuaranteeService;
	
	@Autowired
	public ContractGuaranteeDeleteProcessor(IdmContractGuaranteeService contractGuaranteeService) {
		super(ContractGuaranteeEventType.DELETE);
		//
		Assert.notNull(contractGuaranteeService);
		//
		this.contractGuaranteeService = contractGuaranteeService;
	}

	@Override
	public EventResult<IdmContractGuaranteeDto> process(EntityEvent<IdmContractGuaranteeDto> event) {
		IdmContractGuaranteeDto dto = event.getContent();
		//
		contractGuaranteeService.deleteInternal(dto);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
