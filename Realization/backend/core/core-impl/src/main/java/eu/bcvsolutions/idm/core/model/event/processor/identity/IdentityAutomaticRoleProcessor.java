package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Processor recalculate automatic roles by attribute after save identify or identity eav's.
 * 
 * @author Ondrej Kopr
 *
 */

@Component
@Description("Recalculate automatic roles by attribute after save identity.")
public class IdentityAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityDto> implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-automatic-role-processor";

	private final IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	private final IdmIdentityContractService identityContractService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public IdentityAutomaticRoleProcessor(
			IdmAutomaticRoleAttributeService automaticRoleAttributeService,
			IdmIdentityContractService identityContractService) {
		super(IdentityContractEventType.NOTIFY);
		//
		Assert.notNull(automaticRoleAttributeService, "Service is required.");
		Assert.notNull(identityContractService, "Service is required.");
		//
		this.automaticRoleAttributeService = automaticRoleAttributeService;
		this.identityContractService = identityContractService;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		// skip recalculation
		return super.conditional(event)
				&& !getBooleanProperty(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, event.getProperties());
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		//
		IdmIdentityDto identity = event.getContent();
		UUID identityId = identity.getId();
		//
		AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.IDENTITY;
		if (CoreEventType.EAV_SAVE.name().equals(event.getParentType())) {
			type = AutomaticRoleAttributeRuleType.IDENTITY_EAV;
		}
		//
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		//
		for (IdmIdentityContractDto contract : identityContractService.findAllByIdentity(identityId)) {
			UUID contractId = contract.getId();
			Set<AbstractIdmAutomaticRoleDto> allNewPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(true, type, contractId);
			Set<AbstractIdmAutomaticRoleDto> allNotPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(false, type, contractId);
			
			// Iterate over newly passed
			for (AbstractIdmAutomaticRoleDto autoRole : allNewPassedAutomaticRoleForContract) {
				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
				concept.setIdentityContract(contract.getId());
				concept.setValidFrom(contract.getValidFrom());
				concept.setValidTill(contract.getValidTill());
				concept.setRole(autoRole.getRole());
				concept.setAutomaticRole(autoRole.getId());
				concept.setOperation(ConceptRoleRequestOperation.ADD);
				concepts.add(concept);
			}
			//
			// Iterate over newly not passed
			for (AbstractIdmAutomaticRoleDto autoRole : allNotPassedAutomaticRoleForContract) {
				//
				// Find all identity roles
				IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
				filter.setAutomaticRoleId(autoRole.getId());
				filter.setIdentityContractId(contractId);
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
		}
		// Execute concepts
		roleRequestService.executeConceptsImmediate(identityId, concepts);
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
