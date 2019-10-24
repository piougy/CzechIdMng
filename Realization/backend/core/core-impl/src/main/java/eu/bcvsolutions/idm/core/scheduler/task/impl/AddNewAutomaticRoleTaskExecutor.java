package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Long running task for add newly added automatic role to users. 
 * Can be executed repetitively to assign role to unprocessed identities, after process was stopped or interrupted (e.g. by server restart). 
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @author Jan Helbich
 *
 */
@Component(AddNewAutomaticRoleTaskExecutor.TASK_NAME)
@Description("Add new automatic role by tree structure for existing identity contacts. "
		+ "Can be executed repetitively to assign role to unprocessed identities, "
		+ "after process was stopped or interrupted (e.g. by server restart).")
public class AddNewAutomaticRoleTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityContractDto> {

	public static final String TASK_NAME = "core-add-new-automatic-role-contract-long-running-task";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AddNewAutomaticRoleTaskExecutor.class);
	//
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	//
	private UUID roleTreeNodeId = null;
	private IdmRoleTreeNodeDto roleTreeNode = null;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.roleTreeNode = null;
		this.setAutomaticRoleId(getParameterConverter().toUuid(properties, AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE));
	}
	
	@Override
	public boolean isInProcessedQueue(IdmIdentityContractDto dto) {
		// we want to log items, but we want to execute them every times
		return false;
	}
	
	@Override
	public Page<IdmIdentityContractDto> getItemsToProcess(Pageable pageable) {
		List<IdmIdentityContractDto> contracts = identityContractService
				.findAllByWorkPosition(getRoleTreeNode().getTreeNode(), getRoleTreeNode().getRecursionType());
		return new PageImpl<>(contracts);
	}
	
	@Override
	public Optional<OperationResult> processItem(IdmIdentityContractDto contract) {
		try {
			if (!contract.isValidNowOrInFuture()) {
				IdmIdentityDto identity = DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity);
				IdmRoleDto role = DtoUtils.getEmbedded(getRoleTreeNode(), IdmRoleTreeNode_.role);
				return Optional.of(new OperationResult
						.Builder(OperationState.NOT_EXECUTED)
						.setModel(new DefaultResultModel(
								CoreResultCode.AUTOMATIC_ROLE_CONTRACT_IS_NOT_VALID,
								ImmutableMap.of(
										"role", role.getCode(),
										"roleTreeNode", getRoleTreeNode().getId(),
										"identity", identity.getUsername())))
						.build());
			}
			List<IdmIdentityRoleDto> allByContract = identityRoleService.findAllByContract(contract.getId());
			//
			// skip already assigned automatic roles
			for (IdmIdentityRoleDto roleByContract : allByContract) {
				if (ObjectUtils.equals(roleByContract.getAutomaticRole(), getRoleTreeNode().getId())) {
					IdmIdentityDto identity = DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity);
					IdmRoleDto role = DtoUtils.getEmbedded(getRoleTreeNode(), IdmRoleTreeNode_.role);
					return Optional.of(new OperationResult
							.Builder(OperationState.NOT_EXECUTED)
							.setModel(new DefaultResultModel(
									CoreResultCode.AUTOMATIC_ROLE_ALREADY_ASSIGNED,
									ImmutableMap.of(
											"role", role.getCode(),
											"roleTreeNode", getRoleTreeNode().getId(),
											"identity", identity.getUsername())))
							.build());

				}
			}
			//
			// automatic role by tree node is added directly trough identity role
			IdmRoleTreeNodeDto autoRole = getRoleTreeNode();
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(autoRole.getRole());
			conceptRoleRequest.setAutomaticRole(autoRole.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
			roleRequestService.executeConceptsImmediate(contract.getIdentity(), Lists.newArrayList(conceptRoleRequest));
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch(Exception ex) {
			IdmIdentityDto identity = DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity);
			IdmRoleDto role = DtoUtils.getEmbedded(getRoleTreeNode(), IdmRoleTreeNode_.role);
			//
			LOG.error("Adding role [{}] by automatic role [{}] for identity [{}] failed",
					role.getCode(), getRoleTreeNode().getId(), identity.getUsername(), ex);
			//
			return Optional.of(new OperationResult
					.Builder(OperationState.EXCEPTION)
					.setModel(new DefaultResultModel(
							CoreResultCode.AUTOMATIC_ROLE_ASSIGN_TASK_NOT_COMPLETE,
							ImmutableMap.of(
									"role", role.getCode(),
									"roleTreeNode", getRoleTreeNode().getId(),
									"identity", identity.getUsername())))
					.setCause(ex)
					.build());
		}
	}
	
	public void setAutomaticRoleId(UUID automaticRoleId) {
		this.roleTreeNodeId = automaticRoleId;
	}
	
	public UUID getAutomaticRoleId() {
		return this.roleTreeNodeId;
	}
	
	private IdmRoleTreeNodeDto getRoleTreeNode() {
		if (roleTreeNode == null) {
			roleTreeNode = roleTreeNodeService.get(getAutomaticRoleId());
			if (roleTreeNode == null) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
			}
		}
		return roleTreeNode;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, roleTreeNodeId);
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE);
		return propertyNames;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto automaticRoleAttribute = new IdmFormAttributeDto(
				AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE,
				AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, 
				PersistentType.UUID,
				BaseFaceType.AUTOMATIC_ROLE_TREE_SELECT);
		automaticRoleAttribute.setRequired(true);
		//
		return Lists.newArrayList(automaticRoleAttribute);
	}
}
