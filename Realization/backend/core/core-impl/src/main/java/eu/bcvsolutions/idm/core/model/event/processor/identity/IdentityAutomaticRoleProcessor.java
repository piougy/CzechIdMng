package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Processor recalculate automatic roles by attribute after save identify or identity eav's.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Recalculate automatic roles after save identity.")
public class IdentityAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityDto> implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-automatic-role-processor";

	private final IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	private final IdmIdentityContractService identityContractService;
	
	@Autowired
	public IdentityAutomaticRoleProcessor(
			IdmAutomaticRoleAttributeService automaticRoleAttributeService,
			IdmIdentityContractService identityContractService) {
		super(IdentityEventType.UPDATE, IdentityEventType.CREATE, CoreEventType.EAV_SAVE);
		//
		Assert.notNull(automaticRoleAttributeService);
		Assert.notNull(identityContractService);
		//
		this.automaticRoleAttributeService = automaticRoleAttributeService;
		this.identityContractService = identityContractService;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		// skip recalculation
		if (this.getBooleanProperty(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, event.getProperties())) {
			return new DefaultEventResult<>(event, this);
		}
		//
		IdmIdentityDto identity = event.getContent();
		UUID identityId = identity.getId();
		//
		// TODO: one time is possible add this to process queue (like account management)
		for (IdmIdentityContractDto contract : identityContractService.findAllByIdentity(identityId)) {
			UUID contractId = contract.getId();
			Set<AbstractIdmAutomaticRoleDto> allNewPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(true, null, contractId);
			Set<AbstractIdmAutomaticRoleDto> allNotPassedAutomaticRoleForContract = automaticRoleAttributeService.getRulesForContract(false, null, contractId);
			automaticRoleAttributeService.processAutomaticRolesForContract(contractId, allNewPassedAutomaticRoleForContract, allNotPassedAutomaticRoleForContract);
		}
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
		return super.getOrder() + 100;
	}
}
