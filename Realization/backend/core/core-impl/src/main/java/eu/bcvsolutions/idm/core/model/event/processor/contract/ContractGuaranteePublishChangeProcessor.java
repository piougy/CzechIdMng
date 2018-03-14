package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent.ContractGuaranteeEventType;

/**
 * Publish contract guarantee change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish contract guarantee change event.")
public class ContractGuaranteePublishChangeProcessor extends AbstractPublishEntityChangeProcessor<IdmContractGuaranteeDto> {

	public static final String PROCESSOR_NAME = "contract-guarantee-publish-change-processor";
	
	public ContractGuaranteePublishChangeProcessor() {
		super(ContractGuaranteeEventType.CREATE, ContractGuaranteeEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
