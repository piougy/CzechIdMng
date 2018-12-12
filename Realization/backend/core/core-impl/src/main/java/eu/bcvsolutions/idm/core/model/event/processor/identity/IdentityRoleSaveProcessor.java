package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Save identity role
 * 
 * @author Radek Tomiška
 * @author Vít Švanda
 *
 */
@Component(IdentityRoleSaveProcessor.PROCESSOR_NAME)
@Description("Persists identity role.")
public class IdentityRoleSaveProcessor 
		extends CoreEventProcessor<IdmIdentityRoleDto> 
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-save-processor";
	private final IdmIdentityRoleService service;
	private final IdmIdentityRoleValidRequestService validRequestService;
	
	@Autowired
	public IdentityRoleSaveProcessor(
			IdmIdentityRoleService service,
			IdmIdentityRoleValidRequestService validRequestService) {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE);
		//
		Assert.notNull(service);
		Assert.notNull(validRequestService);
		//
		this.service = service;
		this.validRequestService = validRequestService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		identityRole = service.saveInternal(identityRole);
		// Validate form attributes
		validate(identityRole);
		
		event.setContent(identityRole);
		//
		// if identityRole isn't valid save request into validRequests
		if (!EntityUtils.isValid(identityRole)) {
			// create new IdmIdentityRoleValidRequest
			validRequestService.createByIdentityRoleId(identityRole.getId());
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	public void validate(IdmIdentityRoleDto identityRole) {
		List<InvalidFormAttributeDto> validationResults = service.validateFormAttributes(identityRole);
		if (validationResults != null && !validationResults.isEmpty()) {
			IdmRoleDto role = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.role, IdmRoleDto.class);;
			throw new ResultCodeException(CoreResultCode.IDENTITY_ROLE_UNVALID_ATTRIBUTE,
					ImmutableMap.of( //
							"identityRole", identityRole.getId(), //
							"roleCode", role != null ? role.getCode() : "",
							"attributeCode", validationResults.get(0).getAttributeCode() //
							) //
					); //
		}
	}
}