package eu.bcvsolutions.idm.acc.sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Processor save at the end (current) identity role after all identity event types.
 * The behavior is used for check start automatic role recalculation in identity synchronization.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Temporary save identity current roles.")
public class TestIdentityProcessor extends CoreEventProcessor<IdmIdentityDto> implements IdentityProcessor {

	private Map<String, List<IdmIdentityRoleDto>> roles = null;
	private boolean enabled = false;
	
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public TestIdentityProcessor(
			IdmAutomaticRoleAttributeService automaticRoleAttributeService,
			IdmIdentityRoleService identityRoleService) {
		super(IdentityEventType.UPDATE, IdentityEventType.CREATE, CoreEventType.EAV_SAVE);
		//
		this.identityRoleService = identityRoleService;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		//
		if (this.enabled) {
			this.addOrReplaceIdentityRoles(identity);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private void addOrReplaceIdentityRoles(IdmIdentityDto identity) {
		if (this.roles == null) {
			roles = new HashMap<>();
		}
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		roles.put(identity.getUsername(), identityRoles);
	}
	
	@Override
	public int getOrder() {
		// end
		return Integer.MAX_VALUE;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public void disable() {
		this.roles = null;
		this.enabled = false;
	}
	
	public List<IdmIdentityRoleDto> getRolesByUsername(String username) {
		return this.roles.get(username);
	}

	public Map<String, List<IdmIdentityRoleDto>> getRoles() {
		return roles;
	}
}
