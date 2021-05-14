package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Delete given roles.
 *
 * @author svandav
 * @author Ondrej Husnik
 * @author Radek Tomiška
 *
 */
@Component(RoleDeleteBulkAction.NAME)
@Description("Delete given roles.")
public class RoleDeleteBulkAction extends AbstractRemoveBulkAction<IdmRoleDto, IdmRoleFilter> {

	public static final String NAME = "role-delete-bulk-action";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleDeleteBulkAction.class);
	//
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private EntityEventManager entityEventManager;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private EntityStateManager entityStateManager;
	//
	private final List<UUID> processedRoleIds = new ArrayList<UUID>();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLE_DELETE);
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		Set<String> distinctAttributes = new HashSet<>();
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		//
		// add force delete, if currently logged user is ROLE_ADMIN
		if (securityService.hasAnyAuthority(CoreGroupPermission.ROLE_ADMIN)) {
			formAttributes.add(new IdmFormAttributeDto(EntityEventProcessor.PROPERTY_FORCE_DELETE, "Force delete", PersistentType.BOOLEAN));
			distinctAttributes.add(EntityEventProcessor.PROPERTY_FORCE_DELETE);
		}
		//
		// check registered processors and use the setting
		EntityEvent<IdmRoleDto> mockEvent = new RoleEvent(RoleEventType.DELETE, new IdmRoleDto(UUID.randomUUID()));
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
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels result = new ResultModels();

		Map<ResultModel, Long> models = new HashMap<>();
		entities.forEach(roleId -> {
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setRoleId(roleId);
			IdmRoleDto role = getService().get(roleId);
			long count = identityRoleService.count(identityRoleFilter);
			if (count > 0) {
				if (securityService.hasAnyAuthority(CoreGroupPermission.ROLE_ADMIN)) {
					models.put(
							new DefaultResultModel(
									CoreResultCode.ROLE_FORCE_DELETE_BULK_ACTION_NUMBER_OF_IDENTITIES,
									ImmutableMap.of("role", role.getCode(), "count", count)
							),
							count
					);					
				} else {
					models.put(
							new DefaultResultModel(
									CoreResultCode.ROLE_DELETE_BULK_ACTION_NUMBER_OF_IDENTITIES,
									ImmutableMap.of("role", role.getCode(), "count", count)
							),
							count
					);
				}
			}
		});
		
		long conceptsToModify = entities //
				.stream() //
				.map(roleId -> {
					IdmConceptRoleRequestFilter roleRequestFilter = new IdmConceptRoleRequestFilter();
					roleRequestFilter.setRoleId(roleId);
					return conceptRoleRequestService.count(roleRequestFilter);
				})
				.reduce(0L, Long::sum);

		ResultModel conceptCountResult = null;
		if (conceptsToModify > 0) {
			conceptCountResult = new DefaultResultModel(CoreResultCode.ROLE_DELETE_BULK_ACTION_CONCEPTS_TO_MODIFY,
					ImmutableMap.of("conceptCount", conceptsToModify));
		}
		
		// Sort by count
		List<Entry<ResultModel, Long>> collect = models //
				.entrySet() //
				.stream() //
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //
				.limit(5) //
				.collect(Collectors.toList()); //
		collect.forEach(entry -> {
			result.addInfo(entry.getKey());
		});
		
		if (conceptCountResult != null) {
			result.addInfo(conceptCountResult);
		}

		return result;
	}
	
	@Override
	protected OperationResult processDto(IdmRoleDto role) {
		boolean forceDelete = getParameterConverter().toBoolean(getProperties(), EntityEventProcessor.PROPERTY_FORCE_DELETE, false);
		if (!forceDelete) {
			return super.processDto(role);
		}
		// force delete - without request by event
		try {
			// force delete can execute role admin only
			getService().checkAccess(role, IdmBasePermission.ADMIN);
			//
			RoleEvent roleEvent = new RoleEvent(RoleEventType.DELETE, role, new ConfigurationMap(getProperties()).toMap());
			roleEvent.setPriority(PriorityType.HIGH);
			EventContext<IdmRoleDto> result = roleService.publish(roleEvent);
			processedRoleIds.add(result.getContent().getId());
			//
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch (ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch (Exception ex) {
			Throwable resolvedException = ExceptionUtils.resolveException(ex);
			if (resolvedException instanceof ResultCodeException) {
				return new OperationResult.Builder(OperationState.EXCEPTION) //
						.setException((ResultCodeException) resolvedException) //
						.build(); //
			}
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception exception) {
		if (exception != null 
				|| (result != null && OperationState.EXECUTED != result.getState())) {
			return super.end(result, exception);
		}
		// success
		boolean forceDelete = isForceDelete();
		//
		if (forceDelete) {
			for (UUID roleId : processedRoleIds) {
				IdmRoleDto role = roleService.get(roleId);
				if (role != null) {
					// check assigned roles again - can be assigned in the meantime ...
					IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
					identityRoleFilter.setRoleId(roleId);
					if (identityRoleService.count(identityRoleFilter) > 0) {		
						return super.end(
								result, 
								new ResultCodeException(
										CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, 
										ImmutableMap.of("role", role.getCode())
								)
						);
					}
					roleService.deleteInternal(role);
					//
					LOG.debug("Role [{}] deleted.", role.getCode());
				} else {
					LOG.debug("Role [{}] already deleted.", roleId);
				}
				// clean up all states
				entityStateManager.deleteStates(new IdmRoleDto(roleId), null, null);
			}
		}
		return super.end(result, exception);
	}

	@Override
	public ReadWriteDtoService<IdmRoleDto, IdmRoleFilter> getService() {
		return roleService;
	}
}
