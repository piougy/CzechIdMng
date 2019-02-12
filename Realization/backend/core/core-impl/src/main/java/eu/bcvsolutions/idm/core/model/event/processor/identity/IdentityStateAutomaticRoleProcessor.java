package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Processor that recalculate all automatic role for identity after identity
 * state was changed from disable to active. This is required for example by
 * automatic roles that has rules only for identity eavs.
 *
 * The processor isn't executed for create operation.
 *
 * @author Ondrej Kopr
 *
 */

@Component
@Description("Recalculate automatic roles by attribute after identity state was changed.")
public class IdentityStateAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;

	public IdentityStateAutomaticRoleProcessor() {
		super(IdentityEventType.UPDATE);
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		
		IdmIdentityDto identityDto = event.getContent();

		// Iterate over all contracts and recalculate roles
		for (IdmIdentityContractDto contract : identityContractService.findAllByIdentity(identityDto.getId())) {
			UUID contractId = contract.getId();
			// There is required recalculate all roles, second parameters in method getRulesForContract means what types automatic role attributes will be 
			// recalculated. Null parameters means all
			Set<AbstractIdmAutomaticRoleDto> allNewPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(true, null, contractId);
			Set<AbstractIdmAutomaticRoleDto> allNotPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(false, null, contractId);
			// It is required call internal process, because original method requires new transaction and in these transaction isn't contract in new state.
			automaticRoleAttributeService.processAutomaticRolesForContractInternal(contractId, allNewPassedAutomaticRoleForContract, allNotPassedAutomaticRoleForContract);
		}

		// There is recalculate for all auto role, add skip for another processor. It is useless recalculate automatic roles again
		event.getProperties().put(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, Boolean.TRUE);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		return changeStateToValid(event.getContent(), event.getOriginalSource())
				&& !getBooleanProperty(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, event.getProperties());
	}

	@Override
	public int getOrder() {
		// after save
		return super.getOrder() + 100;
	}

	/**
	 * Method check if states of given identities are different. If original identity is null (CREATE)
	 * return true (identity state was changed)
	 *
	 * @param newIdentity
	 * @param originalDto
	 * @return
	 */
	private boolean changeStateToValid(Disableable newIdentity, Disableable originalDto) {
		if (originalDto == null) {
			return true;
		}
		return ObjectUtils.notEqual(newIdentity.isDisabled(), originalDto.isDisabled());
	}
}
