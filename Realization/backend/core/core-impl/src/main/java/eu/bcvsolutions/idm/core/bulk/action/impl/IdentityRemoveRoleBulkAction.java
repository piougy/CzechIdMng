package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Remove role from given identities by bulk operation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("identityRemoveRoleBulkAction")
@Description("Remove role from given identities.")
public class IdentityRemoveRoleBulkAction extends AbstractIdentityBulkAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRemoveRoleBulkAction.class);

	public static final String NAME = "identity-remove-role-bulk-action";

	public static final String ROLE_CODE = "role";
	public static final String PRIMARY_CONTRACT_CODE = "mainContract";
	public static final String APPROVE_CODE = "approve";
	
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Override
	protected OperationResult processIdentity(IdmIdentityDto identity) {
		List<IdmIdentityContractDto> contracts = new ArrayList<>();
		if (this.isPrimaryContract()) {
			IdmIdentityContractDto contract = identityContractService.getPrimeValidContract(identity.getId());
			if (contract != null) {
				contracts.add(contract);
			}
		} else {
			contracts.addAll(identityContractService.findAllByIdentity(identity.getId()));
		}
		//
		// contract empty return not processed
		if (contracts.isEmpty()) {
			LOG.warn("For identity id: [{}] username: [{}] wasn't found contranct.", identity.getId(), identity.getUsername());
			return new OperationResult.Builder(OperationState.NOT_EXECUTED)
					.setModel(
							new DefaultResultModel(CoreResultCode.BULK_ACTION_CONTRACT_NOT_FOUND,
									ImmutableMap.of("identity", identity.getId()))) //
					.build();
		}
		boolean approve = isApprove();
		List<IdmRoleDto> roles = getRoles();
		Set<UUID> rolesIds = roles.stream().map(IdmRoleDto::getId).collect(Collectors.toSet());
		//
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>();
		for (IdmIdentityContractDto contract : contracts) {
			if (!checkPermissionForContract(contract)) {
				continue;
			}
			// check if contract has role
			List<IdmIdentityRoleDto> allByContract = identityRoleService.findAllByContract(contract.getId());
			Set<UUID> roleIdsSet = allByContract.stream().map(IdmIdentityRoleDto::getRole).collect(Collectors.toSet());
			if (Collections.disjoint(roleIdsSet, rolesIds)) {
				// contract hasn't assigned the roles
				continue;
			}
			//
			for (IdmRoleDto role : roles) {
				IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
				filter.setIdentityContractId(contract.getId());
				filter.setRoleId(role.getId());
				//
				List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
				if (identityRoles.isEmpty()) {
					continue;
				}
				//
				for (IdmIdentityRoleDto identityRole : identityRoles) {
					IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
					concept.setRole(role.getId());
					concept.setIdentityContract(contract.getId());
					concept.setIdentityRole(identityRole.getId());
					concept.setOperation(ConceptRoleRequestOperation.REMOVE);
					concepts.add(concept);
				}
			}
		}
		
		if (!concepts.isEmpty()) {
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicant(identity.getId());
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequest.setLog("Request was created by bulk action.");
			roleRequest.setExecuteImmediately(!approve); // if set approve, dont execute immediately
			roleRequest = roleRequestService.save(roleRequest, IdmBasePermission.CREATE);
			//
			for (IdmConceptRoleRequestDto concept : concepts) {
				concept.setRoleRequest(roleRequest.getId());
				concept = conceptRoleRequestService.save(concept, IdmBasePermission.CREATE);
			}
			//
			roleRequest = roleRequestService.startRequestInternal(roleRequest.getId(), true);
			if (roleRequest.getState() == RoleRequestState.EXECUTED) {
				return new OperationResult.Builder(OperationState.EXECUTED).build();
			} else {
				return new OperationResult.Builder(OperationState.CREATED).build();
			}
		}
		//
		return new OperationResult.Builder(OperationState.NOT_EXECUTED).build();
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getRoleAttribute());
		formAttributes.add(getApproveAttribute());
		formAttributes.add(getPrimaryContractAttribute());
		return formAttributes;
	}

	@Override
	protected BasePermission[] getPermissionForIdentity() {
		BasePermission[] permissions =  {
				IdentityBasePermission.CHANGEPERMISSION,
				IdmBasePermission.READ
		};
		return permissions;
	}

	@Override
	public Map<String, BasePermission[]> getPermissions() {
		Map<String, BasePermission[]> permissions = super.getPermissions();
		permissions.put(IdmIdentityContract.class.getSimpleName(), this.getPermissionForContract());
		return permissions;
	}

	/**
	 * Check permission for given contract
	 *
	 * @param contract
	 * @return
	 */
	private boolean checkPermissionForContract(IdmIdentityContractDto contract) {
		return PermissionUtils.hasAnyPermission(identityContractService.getPermissions(contract), getPermissionForContract());
	}

	/**
	 * Get permission for contract
	 *
	 * @return
	 */
	private BasePermission[] getPermissionForContract() {
		BasePermission[] permissions = {
				IdmBasePermission.READ,
				IdmBasePermission.AUTOCOMPLETE
		};
		return permissions;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	/**
	 * Get roles for assign
	 *
	 * @return
	 */
	private List<IdmRoleDto> getRoles() {
		Object rolesAsObject = this.getProperties().get(ROLE_CODE);
		//
		if (rolesAsObject == null) {
			return Collections.emptyList();
		}
		//
		if (!(rolesAsObject instanceof Collection)) {
			return Collections.emptyList();
		}
		List<IdmRoleDto> roles = new ArrayList<>();
		((Collection<?>) rolesAsObject).forEach(role -> {
			UUID uuid = EntityUtils.toUuid(role);
			IdmRoleDto roleDto = roleService.get(uuid);
			if (roleDto == null) {
				LOG.warn("Role with id [{}] not found. The role will be skipped.", uuid);
			} else {
				roles.add(roleService.get(uuid));
			}
		});
		return roles;
	}
	
	/**
	 * Is set approve
	 *
	 * @return
	 */
	private boolean isApprove() {
		Boolean approve = this.getParameterConverter().toBoolean(getProperties(), APPROVE_CODE);
		return approve != null ? approve.booleanValue() : true;
	}
	
	/**
	 * Is set only primary contract
	 *
	 * @return
	 */
	private boolean isPrimaryContract() {
		Boolean approve = this.getParameterConverter().toBoolean(getProperties(), PRIMARY_CONTRACT_CODE);
		return approve != null ? approve.booleanValue() : true;
	}
	
	/**
	 * Get {@link IdmFormAttributeDto} for select roles
	 *
	 * @return
	 */
	private IdmFormAttributeDto getRoleAttribute() {
		IdmFormAttributeDto roles = new IdmFormAttributeDto(
				ROLE_CODE, 
				ROLE_CODE, 
				PersistentType.UUID);
		roles.setFaceType(BaseFaceType.ROLE_SELECT);
		roles.setMultiple(true);
		roles.setRequired(true);
		return roles;
	}

	/**
	 * Get {@link IdmFormAttributeDto} for checkbox primary contract
	 *
	 * @return
	 */
	private IdmFormAttributeDto getPrimaryContractAttribute() {
		IdmFormAttributeDto primaryContract = new IdmFormAttributeDto(
				PRIMARY_CONTRACT_CODE, 
				PRIMARY_CONTRACT_CODE, 
				PersistentType.BOOLEAN);
		primaryContract.setDefaultValue(Boolean.TRUE.toString());
		return primaryContract;
	}

	/**
	 * Get {@link IdmFormAttributeDto} for approve checkbox
	 *
	 * @return
	 */
	private IdmFormAttributeDto getApproveAttribute() {
		IdmFormAttributeDto approve = new IdmFormAttributeDto(
				APPROVE_CODE, 
				APPROVE_CODE, 
				PersistentType.BOOLEAN);
		approve.setDefaultValue(Boolean.TRUE.toString());
		return approve;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}
}
