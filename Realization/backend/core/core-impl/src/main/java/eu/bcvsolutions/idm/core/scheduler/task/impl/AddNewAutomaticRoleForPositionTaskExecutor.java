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
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
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
@Service
@Description("Add new automatic role by tree structure for existing contact positionss. "
		+ "Can be executed repetitively to assign role to unprocessed identities, "
		+ "after process was stopped or interrupted (e.g. by server restart).")
public class AddNewAutomaticRoleForPositionTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmContractPositionDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AddNewAutomaticRoleForPositionTaskExecutor.class);
	//
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private LookupService lookupService;
	//
	private UUID roleTreeNodeId = null;
	private IdmRoleTreeNodeDto roleTreeNode = null;

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.roleTreeNode = null;
		this.setAutomaticRoleId(getParameterConverter().toUuid(properties, AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE));
	}
	
	@Override
	public Page<IdmContractPositionDto> getItemsToProcess(Pageable pageable) {
		List<IdmContractPositionDto> positions = contractPositionService
				.findAllByWorkPosition(getRoleTreeNode().getTreeNode(), getRoleTreeNode().getRecursionType());
		return new PageImpl<>(positions);
	}
	
	@Override
	public Optional<OperationResult> processItem(IdmContractPositionDto contractPosition) {
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(contractPosition, IdmContractPosition_.identityContract);
		//
		try {			
			if (!contract.isValidNowOrInFuture()) {
				IdmIdentityDto identity = getIdentity(contract);
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
			List<IdmIdentityRoleDto> allByPosition = identityRoleService.findAllByContractPosition(contractPosition.getId());
			//
			// skip already assigned automatic roles
			for (IdmIdentityRoleDto roleByContract : allByPosition) {
				if (ObjectUtils.equals(roleByContract.getAutomaticRole(), getRoleTreeNode().getId())) {
					IdmIdentityDto identity = getIdentity(contract);
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
			// TODO: role attribute service is used - just added new transaction ... why is this needed?
			automaticRoleAttributeService.addAutomaticRoles(contractPosition, Sets.newHashSet(getRoleTreeNode()));
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch(Exception ex) {
			IdmIdentityDto identity = getIdentity(contract);
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
	
	@Deprecated
	public void setRoleTreeNodeId(UUID roleTreeNodeId) {
		this.setAutomaticRoleId(roleTreeNodeId);
	}
	
	@Deprecated
	protected UUID getRoleTreeNodeId() {
		return this.getAutomaticRoleId();
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
	
	private IdmIdentityDto getIdentity(IdmIdentityContractDto contract) {
		IdmIdentityDto identity = DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity, (IdmIdentityDto) null);
		if (identity == null) {
			identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, contract.getIdentity());
		}
		return identity;
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
}
