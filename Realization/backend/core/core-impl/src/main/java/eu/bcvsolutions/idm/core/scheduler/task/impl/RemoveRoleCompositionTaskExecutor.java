package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.scheduler.api.domain.IdmCheckConcurrentExecution;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Long running task for remove assigned roles by given composition from identities.
 *
 * @author Radek Tomiška
 * @since 9.0.0
 */
@IdmCheckConcurrentExecution
@Component(RemoveRoleCompositionTaskExecutor.TASK_NAME)
public class RemoveRoleCompositionTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityRoleDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveRoleCompositionTaskExecutor.class);
	public static final String TASK_NAME = "core-remove-role-composition-long-running-task";
	public static final String PARAMETER_ROLE_COMPOSITION_ID = "role-composition-id";
	//
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
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
	 * Automatic role removal can be start, if previously LRT ended.
	 */
	@Override
	public void validate(IdmLongRunningTaskDto task) {
		super.validate(task);
		//
		// composition is already deleted
		IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
		if (roleComposition == null) {
			throw new EntityNotFoundException(IdmRoleComposition.class, roleCompositionId);
		}
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setOperationState(OperationState.RUNNING);
		filter.setTaskType(AddNewRoleCompositionTaskExecutor.class.getCanonicalName());
		for (UUID longRunningTaskId : getLongRunningTaskService().findIds(filter, PageRequest.of(0, 1))) {
			throw new AcceptedException(CoreResultCode.ROLE_COMPOSITION_RUN_CONCURRENTLY,
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
	public Page<IdmIdentityRoleDto> getItemsToProcess(Pageable pageable) {
		IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
		Assert.notNull(roleComposition, "Role composition is required.");
		//
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleCompositionId(roleComposition.getId());
		//
		return identityRoleService.find(filter, null);
	}
	
	@Override
	public Optional<OperationResult> processItem(IdmIdentityRoleDto identityRole) {
		try {
			List<IdmConceptRoleRequestDto> preparedConcepts = prepareConcepts(new ArrayList<>(), identityRole);
			
			IdmIdentityContractDto contract = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract);
			// TODO: async? it's delete, what about referential integrity?
			roleRequestService.executeConceptsImmediate(contract.getIdentity(), Lists.newArrayList(preparedConcepts));
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			return Optional.of(new OperationResult
					.Builder(OperationState.EXCEPTION)
					.setModel(new DefaultResultModel(
							CoreResultCode.ROLE_COMPOSITION_ASSIGNED_ROLE_REMOVAL_FAILED,
							ImmutableMap.of(
									"identityRole", identityRole.getId().toString())))
					.setCause(ex)
					.build());
		}
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		if (BooleanUtils.isTrue(result) && ex == null) {
			IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
			Assert.notNull(roleComposition, "Role composition is required.");
			//
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setRoleCompositionId(roleComposition.getId());
			//
			long assignedRoles = identityRoleService.find(filter, PageRequest.of(0, 1)).getTotalElements();
			if (assignedRoles != 0) {
				// some assigned role was created in the meantime
				LOG.warn("Remove role composition [{}] is not complete, some identity roles [{}] remains assigned to identities.", 
						roleCompositionId, assignedRoles);
				//
				return super.end(
						result, 
						new ResultCodeException(
								CoreResultCode.ROLE_COMPOSITION_REMOVE_HAS_ASSIGNED_ROLES, 
								ImmutableMap.of(
									"roleCompositionId", roleCompositionId.toString(), 
									"assignedRoles", String.valueOf(assignedRoles))
						)
				);
			}
			//
			LOG.debug("Remove role composition [{}]", roleCompositionId);
			try {
				roleCompositionService.deleteInternal(roleComposition);
				//
				LOG.debug("End: Remove role composition [{}].", roleCompositionId);
				//
			} catch (Exception O_o) {
				LOG.debug("Remove role composition [{}] failed", roleCompositionId, O_o);
				//
				IdmLongRunningTaskDto task = longRunningTaskService.get(getLongRunningTaskId());
				return super.end(
						result, 
						new ResultCodeException(
								CoreResultCode.LONG_RUNNING_TASK_FAILED, 
								ImmutableMap.of(
										"taskId", getLongRunningTaskId(), 
										"taskType", task.getTaskType(),
										ConfigurationService.PROPERTY_INSTANCE_ID, task.getInstanceId()
								)
						)
				);
			}
		}
		//
		return super.end(result, ex);
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
	
	protected void setIdentityRoleService(IdmIdentityRoleService identityRoleService) {
		this.identityRoleService = identityRoleService;
	}
	
	/**
	 * Identity role => one applicant => one request can be executed from prepared contracts.
	 * 
	 * @param preparedConcepts cumulative concepts => will be used to execute request at end
	 * @param identityRole assigned role
	 * @return concepts to remove assigned roles
	 */
	private List<IdmConceptRoleRequestDto> prepareConcepts(
			List<IdmConceptRoleRequestDto> preparedConcepts, 
			IdmIdentityRoleDto identityRole) {
		// remove assigned role by concept
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setIdentityRole(identityRole.getId());
		conceptRoleRequest.setRole(identityRole.getRole());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptRoleRequest.setIdentityContract(identityRole.getIdentityContract());
		conceptRoleRequest.setContractPosition(identityRole.getContractPosition());
		conceptRoleRequest.setAutomaticRole(identityRole.getAutomaticRole());
		conceptRoleRequest.setDirectRole(identityRole.getDirectRole());
		conceptRoleRequest.setRoleComposition(identityRole.getRoleComposition());
		
		
		IdmRoleCompositionFilter compositionFilter = new IdmRoleCompositionFilter();
		compositionFilter.setSuperiorId(identityRole.getRole());
		preparedConcepts.add(conceptRoleRequest); // prevent cycles ...
		roleCompositionService.find(compositionFilter, null)
			.forEach(composition -> {
				IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
				filter.setRoleId(composition.getSub());
				filter.setRoleCompositionId(composition.getId());
				filter.setDirectRoleId(identityRole.getDirectRole());
				identityRoleService
					.find(filter, PageRequest.of(0, 1)) // just one sub role can be removed => other completely same sub roles will be preserved.
					.forEach(subIdentityRole -> {
						// remove all sub
						if (!preparedConcepts
								.stream()
								.map(IdmConceptRoleRequestDto::getIdentityRole)
								.anyMatch(ir -> ir.equals(subIdentityRole.getId()))) {
							prepareConcepts(preparedConcepts, subIdentityRole);
						}
					});
			});
		//
		return preparedConcepts;
	}
}
