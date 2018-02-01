package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Processor recalculate automatic role after save identity contract
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Recalculate automatic roles after save identity contract.")
public class IdentityContractAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContractDto> implements IdentityContractProcessor {

	public static final String PROCESSOR_NAME = "identity-contract-automatic-role-processor";
	
	private final IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	
	@Autowired
	public IdentityContractAutomaticRoleProcessor(
			IdmAutomaticRoleAttributeService automaticRoleAttributeService) {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.CREATE, CoreEventType.EAV_SAVE);
		//
		Assert.notNull(automaticRoleAttributeService);
		//
		this.automaticRoleAttributeService = automaticRoleAttributeService;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto identityContract = event.getContent();
		UUID identityId = identityContract.getIdentity();
		//
		// resolve automatic role by attribute
		Set<AbstractIdmAutomaticRoleDto> allNewPassedAutomaticRoleForIdentity = automaticRoleAttributeService.getAllNewPassedAutomaticRoleForIdentity(identityId);
		Set<AbstractIdmAutomaticRoleDto> allNotPassedAutomaticRoleForIdentity = automaticRoleAttributeService.getAllNotPassedAutomaticRoleForIdentity(identityId);
		automaticRoleAttributeService.processAutomaticRolesForIdentity(identityId, allNewPassedAutomaticRoleForIdentity, allNotPassedAutomaticRoleForIdentity);
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
