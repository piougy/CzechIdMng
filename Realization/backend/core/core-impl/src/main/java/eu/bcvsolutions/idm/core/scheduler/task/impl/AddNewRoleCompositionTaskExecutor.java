package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Long running task for add newly added role composition to users. Sub roles defined by this composition will be assigned to identities having superior role.
 * Can be executed repetitively to assign unprocessed roles to identities, after process was stopped or interrupted (e.g. by server restart). 
 *
 * @author Radek Tomiška
 * @since 9.0.0
 */
@DisallowConcurrentExecution
@Component(AddNewRoleCompositionTaskExecutor.TASK_NAME)
public class AddNewRoleCompositionTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmRoleDto> {

	public static final String TASK_NAME = "core-add-new-role-composition-long-running-task";
	public static final String PARAMETER_ROLE_COMPOSITION_ID = "role-composition-id";
	//
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private LookupService lookupService;
	//
	private UUID roleCompositionId = null;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.setRoleCompositionId(getParameterConverter().toUuid(properties, PARAMETER_ROLE_COMPOSITION_ID));
	}
	
	/**
	 * Automatic role addition can be start, if previously LRT ended.
	 */
	@Override
	public void validate(IdmLongRunningTaskDto task) {
		super.validate(task);
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(this.getClass().getCanonicalName());
		filter.setRunning(Boolean.TRUE);
		//
		for (UUID longRunningTaskId : getLongRunningTaskService().findIds(filter, PageRequest.of(0, 1))) {
			throw new AcceptedException(CoreResultCode.ROLE_COMPOSITION_RUN_CONCURRENTLY,
					ImmutableMap.of(
							"taskId", longRunningTaskId.toString(),
							"roleCompositionId", roleCompositionId.toString()
					)
			);
		}
		//
		filter.setTaskType(AddNewRoleCompositionTaskExecutor.class.getCanonicalName());
		for (UUID longRunningTaskId : getLongRunningTaskService().findIds(filter, PageRequest.of(0, 1))) {
			throw new ResultCodeException(CoreResultCode.ROLE_COMPOSITION_RUN_CONCURRENTLY,
					ImmutableMap.of(
							"taskId", longRunningTaskId.toString(),
							"roleCompositionId", roleCompositionId.toString()
					)
			);
		}
	}
	
	/**
	 * Returns superior roles, which should be processed
	 */
	@Override
	public Page<IdmRoleDto> getItemsToProcess(Pageable pageable) {
		IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
		Assert.notNull(roleComposition, "Role composition is required.");
		//
		List<IdmRoleDto> superiorRoles = roleCompositionService
				.findAllSuperiorRoles(roleComposition.getSub())
				.stream()
				.map(composition -> {
					return DtoUtils.getEmbedded(composition, IdmRoleComposition_.superior, IdmRoleDto.class);
				})
				.collect(Collectors.toList());
		return new PageImpl<>(superiorRoles);
	}
	
	@Override
	public Optional<OperationResult> processItem(IdmRoleDto superiorRole) {
		try {
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setRoleId(superiorRole.getId());
			//
			identityRoleService
				.find(filter, null)
				.forEach(identityRole -> {
					IdmIdentityContractDto contract = lookupService.lookupEmbeddedDto(identityRole, IdmIdentityRole_.identityContract);
					// find direct sub roles - other sub roles will be processed by role request automatically
					IdmRoleCompositionFilter compositionFilter = new IdmRoleCompositionFilter();
					compositionFilter.setSuperiorId(identityRole.getRole());
					compositionFilter.setId(roleCompositionId);
					//
					List<IdmConceptRoleRequestDto> concepts = roleCompositionService
							.find(compositionFilter, null)
							.stream()
							.map(subRole -> {
								IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
								conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
								// from concept
								conceptRoleRequest.setValidFrom(identityRole.getValidFrom());
								conceptRoleRequest.setValidTill(identityRole.getValidTill());
								conceptRoleRequest.setIdentityContract(identityRole.getIdentityContract());
								conceptRoleRequest.setContractPosition(identityRole.getContractPosition());
								// from assigned (~changed) sub role
								conceptRoleRequest.setRole(subRole.getSub());
								conceptRoleRequest.setDirectRole(identityRole.getId());
								conceptRoleRequest.setRoleComposition(subRole.getId());
								//
								return conceptRoleRequest;
							})
							.collect(Collectors.toList());
					//
					if (!concepts.isEmpty()) {
						IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
						roleRequest.setConceptRoles(concepts);
						roleRequest.setApplicant(contract.getIdentity());
						roleRequest = roleRequestService.startConcepts(new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest), null);
					}
				});
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			return Optional.of(new OperationResult
					.Builder(OperationState.EXCEPTION)
					.setModel(new DefaultResultModel(
							CoreResultCode.ROLE_COMPOSITION_ASSIGN_ROLE_FAILED,
							ImmutableMap.of(
									"role", superiorRole.getCode())))
					.setCause(ex)
					.build());
		}
	}
	
	public void setRoleCompositionId(UUID roleCompositionId) {
		this.roleCompositionId = roleCompositionId;
	}
	
	public UUID getRoleCompositionId() {
		return this.roleCompositionId;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(PARAMETER_ROLE_COMPOSITION_ID, roleCompositionId);
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(PARAMETER_ROLE_COMPOSITION_ID);
		return propertyNames;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto compositionId = new IdmFormAttributeDto(
				PARAMETER_ROLE_COMPOSITION_ID,
				PARAMETER_ROLE_COMPOSITION_ID, 
				PersistentType.UUID);
		compositionId.setRequired(true);
		//
		return Lists.newArrayList(compositionId);
	}
	
	@Override
	public boolean supportsQueue() {
		return false;
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
