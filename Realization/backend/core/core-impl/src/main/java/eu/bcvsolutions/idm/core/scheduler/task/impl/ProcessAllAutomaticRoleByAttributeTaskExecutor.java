package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

/**
 * Process all identities and their automatic roles by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@Description("Add or remove automatic role from IdmAutomaticRoleAttribute.")
public class ProcessAllAutomaticRoleByAttributeTaskExecutor extends AbstractAutomaticRoleTaskExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ProcessAllAutomaticRoleByAttributeTaskExecutor.class);
	
	private static final int PAGE_SIZE = 100;

	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmIdentityService identityService;
	
	@Override
	public void init(Map<String, Object> properties) {
		// TODO: not implemented yet, recalculate is done for all identities and automatic roles
		// parameter role tree node is not important and it isn't used for now
		this.setAutomaticRoleId(getParameterConverter().toUuid(properties, PARAMETER_ROLE_TREE_NODE));
		super.init(properties);
	}
	
	@Override
	public Boolean process() {
		//
		Page<IdmIdentityDto> result = identityService.find(new PageRequest(0, PAGE_SIZE));
		//
		counter = 0L;
		count = Long.valueOf(result.getTotalElements());
		//
		boolean canContinue = true;
		while (canContinue) {
			for(IdmIdentityDto identity : result) {
				LOG.debug("Resolve automatic roles by attribute, for identity username: [{}]", identity.getUsername());
				// TODO: it will be nice if count decrement if no automatic role wasn't changed
				automaticRoleAttributeService.resolveAutomaticRolesByAttribute(identity.getId());
				counter++;	
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			};
			if (!result.hasNext()) {
				break;
			}
			result = identityService.find(result.nextPageable());
		}
		//
		return Boolean.TRUE;
	}

}
