package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Persists role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists role.")
public class RoleSaveProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "role-save-processor";
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;
	private final IdmRoleService service;
	
	@Autowired
	public RoleSaveProcessor(IdmRoleService service) {
		super(RoleEventType.UPDATE, RoleEventType.CREATE);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto entity = event.getContent();
		IdmRoleDto originalSource = event.getOriginalSource();
		
		UUID roleAttributeDefinition = entity.getIdentityRoleAttributeDefinition();
		UUID originalRoleAttributeDefinition = originalSource.getIdentityRoleAttributeDefinition();
		// Validation - form definition can be changed only if none role-form-attributes exists (for this role)
		if(!Objects.equals(roleAttributeDefinition, originalRoleAttributeDefinition)) {
			IdmRoleFormAttributeFilter roleFormAttributeFilter = new IdmRoleFormAttributeFilter();
			roleFormAttributeFilter.setRole(entity.getId());
			long count = roleFormAttributeService.count(roleFormAttributeFilter);
			if (count > 0) {
				throw new ResultCodeException(CoreResultCode.ROLE_FORM_ATTRIBUTE_CHANGE_DEF_NOT_ALLOWED, ImmutableMap.of("role", entity.getCode()));
			}
		}
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}

}
