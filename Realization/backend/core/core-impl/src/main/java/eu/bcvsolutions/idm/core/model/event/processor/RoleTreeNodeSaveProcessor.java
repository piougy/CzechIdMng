package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;

/**
 * Persists automatic role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists automatic role.")
public class RoleTreeNodeSaveProcessor extends CoreEventProcessor<IdmRoleTreeNode> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-save-processor";
	private final IdmRoleTreeNodeRepository repository;
	
	@Autowired
	public RoleTreeNodeSaveProcessor(IdmRoleTreeNodeRepository repository) {
		super(RoleTreeNodeEventType.UPDATE, RoleTreeNodeEventType.CREATE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleTreeNode> process(EntityEvent<IdmRoleTreeNode> event) {
		IdmRoleTreeNode entity = event.getContent();
		//
		repository.save(entity);
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}

}
