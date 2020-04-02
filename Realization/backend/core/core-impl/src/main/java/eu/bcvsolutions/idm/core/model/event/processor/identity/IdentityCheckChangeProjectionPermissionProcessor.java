package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Check currently logged user can change identity form projection.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component(IdentityCheckChangeProjectionPermissionProcessor.PROCESSOR_NAME)
@Description("Check currently logged user can change identity form projection.")
public class IdentityCheckChangeProjectionPermissionProcessor 
		extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-check-change-projection-permission-processor";
	
	@Autowired private IdmIdentityService service;
	
	public IdentityCheckChangeProjectionPermissionProcessor() {
		super(IdentityEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		IdmIdentityDto identity = event.getContent();
		IdmIdentityDto previousIdentity = event.getOriginalSource();
		BasePermission[] permission = event.getPermission();
		//
		// Projection not changes.
		if (Objects.equals(identity.getFormProjection(), previousIdentity.getFormProjection())) {
			return false;
		}
		//
		// Only, when permissions (~UPDATE) has to be evaluated.
		return !PermissionUtils.isEmpty(permission)
				&& PermissionUtils.hasPermission(
						PermissionUtils.toString(Arrays.asList(permission)), 
						IdmBasePermission.UPDATE);
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto previousIdentity = event.getOriginalSource();
		// Previous identity is changed => evaluate access on it instead current.
		service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEPROJECTION);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -20;
	}
}
