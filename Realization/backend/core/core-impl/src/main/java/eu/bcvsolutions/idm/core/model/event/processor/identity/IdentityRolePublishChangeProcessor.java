package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Publish identity role change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish identity role change event.")
public class IdentityRolePublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmIdentityRoleDto>
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-publish-change-processor";
	
	@Autowired private LookupService lookupService;
	
	public IdentityRolePublishChangeProcessor() {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE, CoreEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	protected EntityEvent<IdmIdentityRoleDto> setAdditionalEventProperties(EntityEvent<IdmIdentityRoleDto> event) {
		event = super.setAdditionalEventProperties(event);
		// we need to set super entity owner - identity roles should not be processed concurrently for given identity
		// TODO: can be removed, if account management can be executed concurrently for given identity
		IdmIdentityContractDto identityContract = DtoUtils.getEmbedded(event.getContent(), IdmIdentityRole_.identityContract, (IdmIdentityContractDto) null);
		if (identityContract == null) {
			identityContract = (IdmIdentityContractDto) lookupService.lookupDto(IdmIdentityContractDto.class, event.getContent().getIdentityContract());
		}
		event.setSuperOwnerId(identityContract.getIdentity());
		//
		return event;
	}
}
