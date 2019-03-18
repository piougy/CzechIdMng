package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;

/**
 * Duplicate role - clone automatic roles by tree structure
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(DuplicateRoleAutomaticByTreeProcessor.PROCESSOR_NAME)
@Description("Duplicate role - clone automatic roles by tree structure.")
public class DuplicateRoleAutomaticByTreeProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-duplicate-role-automatic-by-tree-processor";
	public static final String PARAMETER_INCLUDE_AUTOMATIC_ROLE = "include-automatic-role";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private EntityStateManager entityStateManager;
	
	public DuplicateRoleAutomaticByTreeProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto include = new IdmFormAttributeDto(
				PARAMETER_INCLUDE_AUTOMATIC_ROLE,
				"Duplicate automatic roles", 
				PersistentType.BOOLEAN);
		include.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(include);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		return super.conditional(event) 
				&& getBooleanProperty(PARAMETER_INCLUDE_AUTOMATIC_ROLE, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto cloned = event.getContent();
		IdmRoleDto originalSource = event.getOriginalSource();
		
		IdmRoleTreeNodeFilter filter = new IdmRoleTreeNodeFilter();
		filter.setRoleId(cloned.getId());
		Set<UUID> usedAutomaticRoles = new HashSet<>();
		List<IdmRoleTreeNodeDto> currentAutomaticRoles = roleTreeNodeService.find(filter,  null).getContent();
		//
		filter.setRoleId(originalSource.getId());
		roleTreeNodeService
			.find(filter, null)
			.forEach(automaticRole -> {
				UUID exists = exists(currentAutomaticRoles, automaticRole);
				if (exists != null) {
					usedAutomaticRoles.add(exists);
				} else {
					IdmRoleTreeNodeDto clonedAutomaticRole = new IdmRoleTreeNodeDto();
					clonedAutomaticRole.setName(automaticRole.getName());
					clonedAutomaticRole.setRole(cloned.getId());
					clonedAutomaticRole.setTreeNode(automaticRole.getTreeNode());
					clonedAutomaticRole.setRecursionType(automaticRole.getRecursionType());
					//
					RoleTreeNodeEvent automaticRoleEvent = new RoleTreeNodeEvent(RoleTreeNodeEventType.CREATE, clonedAutomaticRole);
					automaticRoleEvent.setPriority(PriorityType.IMMEDIATE); // execute sync
					roleTreeNodeService.publish(automaticRoleEvent, event);
				}
			});
		//
		// remove not used originals
		currentAutomaticRoles
			.stream()
			.filter(automaticRole -> {
				return !usedAutomaticRoles.contains(automaticRole.getId());
			})
			.forEach(automaticRole -> {
				// dirty flag automatic role only - will be processed after parent action ends
				IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
				stateDeleted.setEvent(event.getId());
				stateDeleted.setTransactionId(event.getTransactionId());
				stateDeleted.setSuperOwnerId(cloned.getId());
				stateDeleted.setResult(new OperationResultDto.Builder(OperationState.RUNNING)
						.setModel(new DefaultResultModel(CoreResultCode.DELETED)).build());
				entityStateManager.saveState(automaticRole, stateDeleted);
			});
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 200;
	}
	
	private UUID exists(List<IdmRoleTreeNodeDto> from, IdmRoleTreeNodeDto automaticRole) {
		return from
			.stream()
			.filter(a -> {
				return new EqualsBuilder()
						.append(a.getName(), automaticRole.getName())
						.append(a.getTreeNode(), automaticRole.getTreeNode())
						.append(a.getRecursionType(), automaticRole.getRecursionType())
						.isEquals();
			})
			.findFirst()
			.map(IdmRoleTreeNodeDto::getId)
			.orElse(null);
	}
}
