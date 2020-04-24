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
 * Check currently logged user can change identity basic attributes.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(IdentityCheckBasicAttributePermissionProcessor.PROCESSOR_NAME)
@Description("Check currently logged user can change identity form projection.")
public class IdentityCheckBasicAttributePermissionProcessor 
		extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-check-basic-attribute-permission-processor";
	
	@Autowired private IdmIdentityService service;
	
	public IdentityCheckBasicAttributePermissionProcessor() {
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
		//
		// Only, when permissions (~UPDATE) has to be evaluated.
		BasePermission[] permission = event.getPermission();
		return !PermissionUtils.isEmpty(permission)
				&& PermissionUtils.hasPermission(
						PermissionUtils.toString(Arrays.asList(permission)), 
						IdmBasePermission.UPDATE);
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto previousIdentity = event.getOriginalSource();
		IdmIdentityDto identity = event.getContent();
		// Projection changes.
		if (!Objects.equals(identity.getFormProjection(), previousIdentity.getFormProjection())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEPROJECTION);
		}
		// username changes
		if (!Objects.equals(identity.getUsername(), previousIdentity.getUsername())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEUSERNAME);
		}
		// name changes
		if (!Objects.equals(identity.getFirstName(), previousIdentity.getFirstName())
				|| !Objects.equals(identity.getLastName(), previousIdentity.getLastName())
				|| !Objects.equals(identity.getTitleBefore(), previousIdentity.getTitleBefore())
				|| !Objects.equals(identity.getTitleAfter(), previousIdentity.getTitleAfter())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGENAME);
		}
		// phone changes
		if (!Objects.equals(identity.getPhone(), previousIdentity.getPhone())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEPHONE);
		}
		// email changes
		if (!Objects.equals(identity.getEmail(), previousIdentity.getEmail())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEEMAIL);
		}
		// external code changes
		if (!Objects.equals(identity.getExternalCode(), previousIdentity.getExternalCode())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEEXTERNALCODE);
		}
		// description changes
		if (!Objects.equals(identity.getDescription(), previousIdentity.getDescription())) {
			service.checkAccess(previousIdentity, IdentityBasePermission.CHANGEDESCRIPTION);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -20;
	}
}
