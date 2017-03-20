package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Persists automatic role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists automatic role.")
public class RoleTreeNodeSaveProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-save-processor";
	@Autowired
	private IdmRoleTreeNodeService service;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdentityContractCreateByAutomaticRoleProcessor createByAutomaticRoleProcessor;
	
	public RoleTreeNodeSaveProcessor() {
		super(RoleTreeNodeEventType.CREATE); // update is not supported
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto dto = event.getContent();
		//
		dto = service.saveInternal(dto);
		event.setContent(dto);
		IdmRoleTreeNode entity = service.get(dto.getId());
		//
		// assign role by this added automatic role to all existing identity contracts
		// TODO: long running task
		// TODO: integrate with role request api
		// TODO: optional remove by logged user input
		identityContractService.getContractsByWorkPosition(dto.getTreeNode(), dto.getRecursionType()).forEach(identityContract -> {
			createByAutomaticRoleProcessor.assignAutomaticRoles(identityContract, Sets.newHashSet(entity));
		});
		//		
		return new DefaultEventResult<>(event, this);
	}
}
