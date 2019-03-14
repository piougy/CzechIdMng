package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent.RoleCompositionEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Duplicate role - composition and recursion.
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(DuplicateRoleCompositionProcessor.PROCESSOR_NAME)
@Description("Duplicate role - composition and recursion.")
public class DuplicateRoleCompositionProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DuplicateRoleCompositionProcessor.class);
	//
	public static final String PROCESSOR_NAME = "core-duplicate-role-composition-processor";
	public static final String PARAMETER_INCLUDE_ROLE_COMPOSITION = "include-role-composition";
	//
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private EntityStateManager entityStateManager;
	
	public DuplicateRoleCompositionProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	/**
	 * Returns true, when role should be cloned recursively
	 * - can be overriden, if some role hasn't be cloned recursively, if doesn't exist on the target environment before.
	 * 
	 * @param event processed event
	 * @param originalSubRole original sub role
	 * @param targetSubRole duplicate sub role. {@code null} if target role has to be created. 
	 * @return
	 */
	public boolean duplicateRecursively(EntityEvent<IdmRoleDto> event, IdmRoleDto originalSubRole, IdmRoleDto targetSubRole) {
		/**
		 * e.g. - duplicate only business subroles (if it not new)
		 */
		return true;
	}
	
	/**
	 * Returns true, when role composition should be included in the target role
	 * - can be overriden, if some role hasn't be cloned recursively, if doesn't have the same environment etc.
	 * 
	 * @param event processed event
	 * @param composition source composition
	 * @return
	 */
	public boolean includeComposition(EntityEvent<IdmRoleDto> event, IdmRoleCompositionDto composition) {
		/**
		 * e.g. if some role hasn't be cloned recursively, if doesn't have the same environment
		 */
		return true;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto include = new IdmFormAttributeDto(
				PARAMETER_INCLUDE_ROLE_COMPOSITION,
				"Duplicate sub roles (by business role definition)", 
				PersistentType.BOOLEAN);
		include.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(include);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		return super.conditional(event) 
				&& getBooleanProperty(PARAMETER_INCLUDE_ROLE_COMPOSITION, event.getProperties());
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto cloned = event.getContent();
		IdmRoleDto originalSource = event.getOriginalSource();
		//
		Map<String, Serializable> props = resolveProperties(event);
		Set<UUID> processedRoles = (Set<UUID>) props.get(RoleEvent.PROPERTY_PROCESSED_ROLES);
		processedRoles.add(cloned.getId());
		//
		// find and clone business role composition
		// clone roles recursively
		Set<String> processedSubRoles = new HashSet<>();
		Map<String, IdmRoleCompositionDto> currentSubRoles = new HashMap<>();
		roleCompositionService
			.findDirectSubRoles(cloned.getId())
			.forEach(composition -> {
				IdmRoleDto subRole = DtoUtils.getEmbedded(composition, IdmRoleComposition_.sub);
				currentSubRoles.put(subRole.getCode(), composition);
			});
		//
		roleCompositionService
			.findDirectSubRoles(originalSource.getId())
			.stream()
			.filter(composition -> {
				return includeComposition(event, composition);
			})
			.forEach(composition -> {
				// find sub role on the target environment
				IdmRoleDto subRole = DtoUtils.getEmbedded(composition, IdmRoleComposition_.sub);
				IdmRoleDto targetRole = roleService.getByBaseCodeAndEnvironment(subRole.getBaseCode(), cloned.getEnvironment());
				// 
				if (targetRole != null || duplicateRecursively(event, subRole, targetRole)) {
					if (targetRole == null) {
						targetRole = prepareRole(subRole.getBaseCode(), cloned.getEnvironment()); // new clone
					}
					if (targetRole != null && subRole.getId().equals(targetRole.getId())) {
						LOG.debug("Role [{}] is duplicated on the same environment - skipping recursion for the same roles", targetRole.getCode());
					} else if (targetRole != null && processedRoles.contains(targetRole.getId())) {
						LOG.debug("Role [{}] was already processed by other business role composition - cycle, skipping", targetRole.getCode());
					} else {
						//
						// clone / update
						EntityEvent<IdmRoleDto> subEvent = new RoleEvent(RoleEventType.DUPLICATE, targetRole, props);
						subEvent.setOriginalSource(subRole); // original source is the cloned role
						subEvent.setPriority(PriorityType.IMMEDIATE); // we want to be sync
						EventContext<IdmRoleDto> resultSubRole = roleService.publish(subEvent, event);
						targetRole = resultSubRole.getContent();
					}
					//
					// create the composition (or check composition exists)
					// find exists
					processedSubRoles.add(targetRole.getCode());
					if (!currentSubRoles.containsKey(targetRole.getCode())) {
						IdmRoleCompositionDto cloneComposition = new IdmRoleCompositionDto(cloned.getId(), targetRole.getId());
						EntityEvent<IdmRoleCompositionDto> createCompositionEvent = new RoleCompositionEvent(RoleCompositionEventType.CREATE, cloneComposition);
						createCompositionEvent.setPriority(PriorityType.IMMEDIATE); // we want to be sync
						roleCompositionService.publish(createCompositionEvent, event);
					}
				}
			});
		//
		// remove unprocessed sub roles, which was removed in surce role
		currentSubRoles
			.entrySet()
			.stream()
			.filter(entry -> {
				return !processedSubRoles.contains(entry.getKey());
			})
			.filter(entry -> {
				return includeComposition(event, entry.getValue());
			})
			.forEach(entry -> {
				// dirty flag role composition only - will be processed after parent action ends
				IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
				stateDeleted.setEvent(event.getId());
				stateDeleted.setTransactionId(event.getTransactionId());
				stateDeleted.setSuperOwnerId(cloned.getId());
				stateDeleted.setResult(new OperationResultDto.Builder(OperationState.RUNNING)
						.setModel(new DefaultResultModel(CoreResultCode.DELETED)).build());
				entityStateManager.saveState(entry.getValue(), stateDeleted);
			});
		
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 100;
	}
	
	/**
	 * Prepare new role on the target environment - role with given code doesn't exist jet.
	 * 
	 * @param baseCode
	 * @param environment
	 * @return
	 */
	private IdmRoleDto prepareRole(String baseCode, String environment) {
		IdmRoleDto cloned = new IdmRoleDto();
		cloned.setId(UUID.randomUUID());
		cloned.setBaseCode(baseCode);
		cloned.setEnvironment(environment);
		//
		return cloned;
	}
	
	/**
	 * Creates or reassign processed identity roles @Set of @UUID into event properties.
	 * 
	 * @param event
	 * @return
	 */
	private Map<String, Serializable> resolveProperties(EntityEvent<?> event) {
		Map<String, Serializable> props = new HashMap<>();
		//
		if (event.getProperties().containsKey(RoleEvent.PROPERTY_PROCESSED_ROLES)) {
			props.put(RoleEvent.PROPERTY_PROCESSED_ROLES, event.getProperties().get(RoleEvent.PROPERTY_PROCESSED_ROLES));
		} else {
			props.put(RoleEvent.PROPERTY_PROCESSED_ROLES, new HashSet<UUID>());
		}
		//
		return props;
	}

}
