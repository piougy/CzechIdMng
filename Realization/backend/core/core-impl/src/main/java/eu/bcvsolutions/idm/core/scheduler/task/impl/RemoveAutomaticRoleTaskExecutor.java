package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * Long running task for remove automatic roles from identities.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */

@Service
@Description("Remove automatic role from IdmRoleTreeNode.")
public class RemoveAutomaticRoleTaskExecutor extends AbstractLongRunningTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveAutomaticRoleTaskExecutor.class);
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityContractService identityContractService;
	//
	private IdmRoleTreeNodeDto roleTreeNode = null;
	
	@Override
	public Boolean process() {
		if (roleTreeNode == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
		}
		//
		// TODO: pageable?
		List<IdmIdentityRoleDto> list = identityRoleService.findByAutomaticRole(roleTreeNode.getId(), null).getContent();
		//
		counter = 0L;
		count = Long.valueOf(list.size());
		//
		IdmRole role = roleService.get(roleTreeNode.getRole());
		LOG.debug("[RemoveAutomaticRoleTaskExecutor] Remove role [{}] by automatic role [{}]. Count: [{}]", role.getCode(), roleTreeNode.getId(), count);		
		//
		List<String> failedIdentities = new ArrayList<>();
		boolean canContinue = true;
		for (IdmIdentityRoleDto identityRole : list) {
			IdmRoleRequestDto roleRequest = roleTreeNodeService.prepareRemoveAutomaticRoles(identityRole, Sets.newHashSet(roleTreeNode));
			roleRequest = roleRequestService.startRequest(roleRequest.getId(), false);
			if (roleRequest.getState() != RoleRequestState.EXCEPTION) {
				counter++;
			} else {
				IdmIdentityContractDto identityContract = identityContractService.get(identityRole.getIdentityContract());
				IdmIdentityDto identity = DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity, IdmIdentityDto.class);
				failedIdentities.add(identity.getUsername());
			}
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		if (!failedIdentities.isEmpty()) {
			LOG.debug("End: Remove role [{}] by automatic role [{}]. Count: [{}/{}]", role.getCode(), roleTreeNode.getId(), counter, count);
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_NOT_COMPLETE, 
					ImmutableMap.of(
							"role", role.getCode(),
							"roleTreeNode", roleTreeNode.getId(),
							"identities", StringUtils.join(failedIdentities, ",")));
		}
		// Find all concepts and remove relation on role tree
		ConceptRoleRequestFilter conceptRequestFilter = new ConceptRoleRequestFilter();
		conceptRequestFilter.setRoleTreeNodeId(roleTreeNode.getId());
		conceptRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
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
		//
		// delete entity
		roleTreeNodeService.deleteInternalById(roleTreeNode.getId());
		LOG.debug("End: Remove role [{}] by automatic role [{}]. Count: [{}/{}]", role.getCode(), roleTreeNode.getId(), counter, count);
		//
		return Boolean.TRUE;
	}

	public void setRoleTreeNode(IdmRoleTreeNodeDto roleTreeNode) {
		this.roleTreeNode = roleTreeNode;
	}

}
