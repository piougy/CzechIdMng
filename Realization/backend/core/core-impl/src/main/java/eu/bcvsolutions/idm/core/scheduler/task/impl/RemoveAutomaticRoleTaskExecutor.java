package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * Long running task for remove automatic roles from identities.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@Description("Remove automatic role from IdmRoleTreeNode.")
public class RemoveAutomaticRoleTaskExecutor extends AbstractLongRunningTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveAutomaticRoleTaskExecutor.class);
	
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	
	@Autowired
	private IdmConceptRoleRequestService conceptRequestService;
	
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Autowired
	private IdmRoleTreeNodeRepository roleTreeNodeRepository;
	
	private IdmRoleTreeNode roleTreeNode = null;
	
	@Override
	public Boolean process() {
		if (roleTreeNode == null) {
			return Boolean.FALSE;
		}
		//
		// TODO: pageable?
		List<IdmIdentityRole> list = identityRoleService.getRolesByAutomaticRole(roleTreeNode.getId(), null).getContent();
		//
		counter = 0L;
		count = Long.valueOf(list.size());
		//
		LOG.debug("[RemoveAutomaticRoleTaskExecutor] Remove automatic roles. Count: [{}]", count);
		//
		boolean canContinue = true;
		for (IdmIdentityRole identityRole : list) {
			roleTreeNodeService.updateOrRemoveAutomaticRoles(identityRole, Sets.newHashSet(roleTreeNode), ConceptRoleRequestOperation.REMOVE, true);
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
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
		roleTreeNodeRepository.delete(roleTreeNode.getId());
		//
		//
		return Boolean.TRUE;
	}

	public IdmRoleTreeNode getRoleTreeNode() {
		return roleTreeNode;
	}

	public void setRoleTreeNode(IdmRoleTreeNode roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}

}
