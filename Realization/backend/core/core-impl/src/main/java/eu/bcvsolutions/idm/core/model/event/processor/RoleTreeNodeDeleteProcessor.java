package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

/**
 * Deletes automatic role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes automatic role.")
public class RoleTreeNodeDeleteProcessor extends CoreEventProcessor<IdmRoleTreeNode> {

	public static final String PROCESSOR_NAME = "role-tree-node-delete-processor";
	private final AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository;
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public RoleTreeNodeDeleteProcessor(
			AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository,
			IdmIdentityRoleService identityRoleService) {
		super(RoleTreeNodeEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		//
		this.repository = repository;
		this.identityRoleService = identityRoleService;
		
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmRoleTreeNode> process(EntityEvent<IdmRoleTreeNode> event) {
		IdmRoleTreeNode roleTreeNode = event.getContent();
		//
		// delete all assigned roles gained by this automatic role
		// TODO: long running task
		// TODO: optional remove by logged user input
		identityRoleService.getRoles(roleTreeNode, null).forEach(identityRole -> {
			identityRoleService.delete(identityRole);
		});
		//
		// delete entity
		repository.delete(roleTreeNode);
		//
		return new DefaultEventResult<>(event, this);
	}
}
