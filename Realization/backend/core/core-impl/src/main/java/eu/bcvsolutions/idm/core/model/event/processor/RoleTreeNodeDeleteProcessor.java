package eu.bcvsolutions.idm.core.model.event.processor;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;

/**
 * Deletes automatic role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes automatic role.")
public class RoleTreeNodeDeleteProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {

	public static final String PROCESSOR_NAME = "role-tree-node-delete-processor";
	private final AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmConceptRoleRequestService conceptRequestService;
	private final IdmRoleRequestService roleRequestService;
	
	@Autowired
	public RoleTreeNodeDeleteProcessor(
			AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository,
			IdmIdentityRoleService identityRoleService, 
			IdmConceptRoleRequestService conceptRequestService,
			IdmRoleRequestService roleRequestService) {
		super(RoleTreeNodeEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		Assert.notNull(conceptRequestService);
		Assert.notNull(roleRequestService);
		//
		this.repository = repository;
		this.identityRoleService = identityRoleService;
		this.conceptRequestService = conceptRequestService;
		this.roleRequestService = roleRequestService;
		
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto roleTreeNode = event.getContent();
		//
		// delete all assigned roles gained by this automatic role
		// TODO: long running task
		// TODO: integrate with role request api
		// TODO: optional remove by logged user input
		identityRoleService.getRolesByAutomaticRole(roleTreeNode.getId(), null).forEach(identityRole -> {
			identityRoleService.delete(identityRole);
		});
		//
		// Find all concepts and remove relation on role tree
		ConceptRoleRequestFilter conceptRequestFilter = new ConceptRoleRequestFilter();
		conceptRequestFilter.setRoleTreeNodeId(roleTreeNode.getId());
		conceptRequestService.findDto(conceptRequestFilter, null).getContent().forEach(concept -> {
			IdmRoleRequestDto request = roleRequestService.getDto(concept.getRoleRequest());
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"Role tree node [{0}] (reqested in concept [{1}]) was deleted (not from this role request)!",
						roleTreeNode.getId(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested RoleTreeNode [{1}] was deleted (not from this role request)!",
						concept.getId(), roleTreeNode.getId());
				concept.setState(RoleRequestState.CANCELED);
			}
			roleRequestService.addToLog(request, message);
			conceptRequestService.addToLog(concept, message);
			concept.setRoleTreeNode(null);

			roleRequestService.save(request);
			conceptRequestService.save(concept);
		});
		
		// delete entity
		repository.delete(roleTreeNode.getId());
		//
		return new DefaultEventResult<>(event, this);
	}
}
