package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType;

/**
 * Identity role valid request processor that remove unless entity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Remove unless entity [IDENTITY_ROLE_VALID].")
public class IdentityRoleValidRequestDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentityRoleValidRequestDto> {
	
	public static final String PROCESSOR_NAME = "identity-role-valid-request-delete-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleValidRequestDeleteProcessor.class);
	private final IdmIdentityRoleValidRequestService validRequestService;
	
	@Autowired
	public IdentityRoleValidRequestDeleteProcessor(
			IdmIdentityRoleValidRequestService validRequestService) {
		super(IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID);
		//
		Assert.notNull(validRequestService);
		//
		this.validRequestService = validRequestService;
	}
	
	@Override
	public EventResult<IdmIdentityRoleValidRequestDto> process(EntityEvent<IdmIdentityRoleValidRequestDto> event) {
		//
		LOG.debug("[IdentityRoleValidRequestDeleteProcessor] Delete IdentityRoleValidRequest with id: [{}]", event.getContent().getId());
		// remove unless identity role valid request
		validRequestService.deleteInternalById(event.getContent().getId());
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}
