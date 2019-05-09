package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Recalculate automatic roles after identity contract is changed
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
@Component
@Description("Recalculate automatic roles after identity contract is changed.")
public class IdentityContractAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContractDto>
		implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-automatic-role-processor";

	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

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
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		//
		AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.CONTRACT;
		// get original event type
		if (CoreEventType.EAV_SAVE.name().equals(event.getParentType())) {
			type = AutomaticRoleAttributeRuleType.CONTRACT_EAV;
		}
		//
		// resolve automatic role by attribute
		Set<AbstractIdmAutomaticRoleDto> allNewPassedAutomaticRoleForContract = automaticRoleAttributeService
				.getRulesForContract(true, type, contractId);
		Set<AbstractIdmAutomaticRoleDto> allNotPassedAutomaticRoleForContract = automaticRoleAttributeService
				.getRulesForContract(false, type, contractId);
		//
		// Iterate over newly passed
		for (AbstractIdmAutomaticRoleDto autoRole : allNewPassedAutomaticRoleForContract) {
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setIdentityContract(identityContract.getId());
			concept.setValidFrom(identityContract.getValidFrom());
			concept.setValidTill(identityContract.getValidTill());
			concept.setRole(autoRole.getRole());
			concept.setAutomaticRole(autoRole.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concepts.add(concept);
		}
		//
		// Iterate over newly not passed
		for (AbstractIdmAutomaticRoleDto autoRole : allNotPassedAutomaticRoleForContract) {
			// Find all identity roles
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setAutomaticRoleId(autoRole.getId());
			filter.setIdentityContractId(contractId);;
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
			//
			for (IdmIdentityRoleDto identityRole : identityRoles) {
				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
				concept.setIdentityContract(contractId);
				concept.setRole(autoRole.getRole());
				concept.setAutomaticRole(autoRole.getId());
				concept.setIdentityRole(identityRole.getId());
				concept.setOperation(ConceptRoleRequestOperation.REMOVE);
				concepts.add(concept);
			}
		}
		//
		// Execute concepts
		roleRequestService.executeConceptsImmediate(identityContract.getIdentity(), concepts);
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
