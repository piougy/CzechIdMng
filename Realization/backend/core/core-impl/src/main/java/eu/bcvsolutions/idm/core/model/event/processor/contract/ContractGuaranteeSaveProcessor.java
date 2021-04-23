package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent.ContractGuaranteeEventType;

/**
 * Processor save {@link IdmContractGuaranteeDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 *
 */

@Component
@Description("Save manually added gurantee for contract.")
public class ContractGuaranteeSaveProcessor extends CoreEventProcessor<IdmContractGuaranteeDto> {

	public static final String PROCESSOR_NAME = "contract-guarantee-save";
	
	@Autowired
	private IdmContractSliceService sliceService;
	private final IdmContractGuaranteeService contractGuaranteeService;
	
	@Autowired
	public ContractGuaranteeSaveProcessor(IdmContractGuaranteeService contractGuaranteeService) {
		super(ContractGuaranteeEventType.CREATE, ContractGuaranteeEventType.UPDATE);
		//
		Assert.notNull(contractGuaranteeService, "Service is required.");
		//
		this.contractGuaranteeService = contractGuaranteeService;
	}

	@Override
	public EventResult<IdmContractGuaranteeDto> process(EntityEvent<IdmContractGuaranteeDto> event) {
		checkControlledBySlices(event);
		
		IdmContractGuaranteeDto dto = event.getContent();
		//
		dto = contractGuaranteeService.saveInternal(dto);
		//
		event.setContent(dto);
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

	/**
	 * Test if contract of the given contract guarantee has some slices.
	 * 
	 * @param guarantee
	 * @return
	 */
	private void checkControlledBySlices(EntityEvent<IdmContractGuaranteeDto> event) {
		IdmContractGuaranteeDto guarantee = event.getContent();
		if(getBooleanProperty(ContractSliceManager.SKIP_CHECK_FOR_SLICES, event.getProperties())) {
			return;
		}
		UUID contract = guarantee.getIdentityContract();
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(contract);
		if (contract != null && sliceService.count(sliceFilter) > 0) {
			throw new ResultCodeException(CoreResultCode.CONTRACT_IS_CONTROLLED_GUARANTEE_CANNOT_BE_MODIFIED,
					ImmutableMap.of("contractId", contract));
		}
	}
}
