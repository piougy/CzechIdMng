package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Recalculate automatic roles after identity contract is changed
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Component
@Description("Recalculate automatic roles after identity contract is changed.")
public class IdentityContractAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContractDto> implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-automatic-role-processor";
	
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	
	public IdentityContractAutomaticRoleProcessor() {
		super(IdentityContractEventType.NOTIFY);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		// skip recalculation
		return super.conditional(event)
				&& !getBooleanProperty(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, event.getProperties());
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto identityContract = event.getContent();
		UUID contractId = identityContract.getId();
		//
		AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.CONTRACT;
		// get original event type
		if (CoreEventType.EAV_SAVE.name().equals(event.getProperties().get(EntityEventManager.EVENT_PROPERTY_PARENT_EVENT_TYPE))) {
			type = AutomaticRoleAttributeRuleType.CONTRACT_EAV;
		}
		//
		// resolve automatic role by attribute
		Set<AbstractIdmAutomaticRoleDto> allNewPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(true, type, contractId);
		Set<AbstractIdmAutomaticRoleDto> allNotPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(false, type, contractId);
		automaticRoleAttributeService.processAutomaticRolesForContract(contractId, allNewPassedAutomaticRoleForContract, allNotPassedAutomaticRoleForContract);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		// after save
		return super.getOrder() + 500;
	}

}
