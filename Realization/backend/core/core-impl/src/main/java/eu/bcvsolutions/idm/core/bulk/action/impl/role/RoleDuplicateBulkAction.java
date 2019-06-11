package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseCodeList;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Duplicate role entry point.
 *
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(RoleDuplicateBulkAction.NAME)
@Description("Duplicate role.")
public class RoleDuplicateBulkAction extends AbstractBulkAction<IdmRoleDto, IdmRoleFilter> {

	public static final String NAME = "core-duplicate-role-bulk-action";
	public static final String PROPERTY_ENVIRONMENT = "environment";
	//
	@Autowired private IdmRoleService roleService;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private EntityStateManager entityStateManager;
	@Autowired private LookupService lookupService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLE_CREATE, CoreGroupPermission.ROLE_UPDATE);
	}
	
	@Override
	public ReadWriteDtoService<IdmRoleDto, IdmRoleFilter> getService() {
		return roleService;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		Set<String> distinctAttributes = new HashSet<>();
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(new IdmFormAttributeDto(PROPERTY_ENVIRONMENT, "Environment", PersistentType.CODELIST, BaseCodeList.ENVIRONMENT));
		distinctAttributes.add(PROPERTY_ENVIRONMENT);
		// check registered processors and use the setting
		EntityEvent<IdmRoleDto> mockEvent = new RoleEvent(RoleEventType.DUPLICATE, new IdmRoleDto(UUID.randomUUID()));
		entityEventManager
			.getEnabledProcessors(mockEvent)
			.forEach(processor -> {
				processor.getFormAttributes().forEach(formAttribute -> {
					if (!distinctAttributes.contains(formAttribute.getCode())) {
						if (StringUtils.isEmpty(formAttribute.getModule())) {
							// attributes can be registered in different module
							formAttribute.setModule(processor.getModule());
						}
						formAttributes.add(formAttribute);
						distinctAttributes.add(formAttribute.getCode());
					}
				});
			});
		//
		return formAttributes;
	}

	@Override
	protected OperationResult processDto(IdmRoleDto dto) {
		IdmRoleDto targetRole;
		String baseCode = dto.getBaseCode();
		String environment = (String) getProperties().get(PROPERTY_ENVIRONMENT);
		if (StringUtils.isEmpty(environment) || Objects.equals(environment, dto.getEnvironment())) { // duplicate on the same environment
			environment = dto.getEnvironment();
			// create role with the same name on the target environment and append index if needed into base code
			baseCode = getUniqueBaseCode(baseCode, environment, 0);
			targetRole = prepareRole(baseCode, environment); // new clone
		} else {
			// try to find role with the same code on the target environment => update role content
			targetRole = roleService.getByBaseCodeAndEnvironment(baseCode, environment);
			if (targetRole == null) {
				targetRole = prepareRole(baseCode, environment); // new clone
			}
		}
		//
		EntityEvent<IdmRoleDto> event = new RoleEvent(RoleEventType.DUPLICATE, targetRole, new ConfigurationMap(getProperties()).toMap());
		event.setOriginalSource(dto); // original source is the cloned role
		event.setPriority(PriorityType.IMMEDIATE); // we want to be sync
		roleService.publish(event, IdmBasePermission.CREATE, IdmBasePermission.UPDATE);
		//
		// delete all states created by event processing - with the same transaction id
		IdmEntityStateFilter stateFilter = new IdmEntityStateFilter();
		stateFilter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		stateFilter.setResultCode(CoreResultCode.DELETED.getCode());
		entityStateManager
			.findStates(stateFilter, null)
			.getContent()
			.forEach(state -> {
				BaseDto owner = lookupService.lookupDto(state.getOwnerType(), state.getOwnerId());
				if (owner != null) {
					EntityEvent<BaseDto> deleteEvent = new CoreEvent<>(CoreEventType.DELETE, owner);
					deleteEvent.setPriority(PriorityType.IMMEDIATE); // we want to be sync
					entityEventManager.process(deleteEvent, event);
				}
				entityStateManager.deleteState(state);
			});
		
		//
		return null;
	}
	
	/**
	 * Duplicate on the same environment => unique code "generator".
	 * 
	 * @param baseCode
	 * @param environment
	 * @param i
	 * @return
	 */
	private String getUniqueBaseCode(String baseCode, String environment, int i) {
		String newCode;
		if (i > 0) {
			newCode = MessageFormat.format("{0}_{1}", baseCode, i);
		} else {
			newCode = baseCode;
		}
		
		if (roleService.getByBaseCodeAndEnvironment(newCode, environment) == null) {
			return newCode;
		}
		return getUniqueBaseCode(baseCode, environment, i + 1);

	}	
	
	/**
	 * Prepare new role on the target environment - role with given code doesn't exist jet.
	 * 
	 * @param baseCode
	 * @param environment
	 * @return
	 */
	private IdmRoleDto prepareRole(String baseCode, String environment) {
		IdmRoleDto duplicate = new IdmRoleDto();
		duplicate.setId(UUID.randomUUID());
		duplicate.setBaseCode(baseCode);
		duplicate.setEnvironment(environment);
		//
		return duplicate;
	}
}
